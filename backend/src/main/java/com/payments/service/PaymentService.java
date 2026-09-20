package com.payments.service;

import com.payments.client.FakeGatewayClient;
import com.payments.client.FakeGatewayClient.GatewayResult;
import com.payments.model.IdempotencyRecord;
import com.payments.model.Payment;
import com.payments.repository.IdempotencyRepository;
import com.payments.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final IdempotencyRepository idemRepo;
    private final FakeGatewayClient gatewayClient;

    public PaymentService(PaymentRepository paymentRepo, 
    IdempotencyRepository idemRepo,
    FakeGatewayClient gatewayClient) {
        this.paymentRepo = paymentRepo;
        this.idemRepo = idemRepo;
        this.gatewayClient = gatewayClient;
    }

    public ResponseEntity process(String key, BigDecimal amount) {
        // 1. Check if key already exists
        var existingOpt = idemRepo.findByIdempotencyKey(key);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getStatus() == IdempotencyRecord.Status.PENDING) {
                return ResponseEntity.status(409).body("{\"error\":\"already processing\"}"); 
            } else {
                return ResponseEntity.ok(existing.getResponse());
            }
        }

        // 2. Claim the key and check for race condition
        try {
            idemRepo.saveAndFlush(new IdempotencyRecord(key, IdempotencyRecord.Status.PENDING));
        } catch (DataIntegrityViolationException e) {
            //race another thread inserted after our first check
            var existingOpt2 = idemRepo.findByIdempotencyKey(key);
            if (existingOpt2.isPresent()) {
                var existing2 = existingOpt2.get();
                if (existing2.getStatus() == IdempotencyRecord.Status.PENDING) {
                    return ResponseEntity.status(409).body("{\"error\":\"already processing\"}"); 
                } else {
                    return ResponseEntity.ok(existing2.getResponse());
                }
            }
        }

        // 3. We own the key, create payment
        Payment payment = new Payment();
        payment.setAmount(amount);
        
        // 4. Send payment to gateway and handle response
        try {
            var result = gatewayClient.charge(amount);
            payment.setStatus(result.status().equals("SUCCESS") ?
            Payment.PaymentStatus.COMPLETED :
            Payment.PaymentStatus.FAILED);
            payment.setGatewayId(result.gatewayId());
            paymentRepo.save(payment);
            String jsonResponse = "{\"id\":\""+ payment.getId() + "\"}";

            var record = idemRepo.findById(key).get();
            record.setResponse(jsonResponse);
            record.setStatus(IdempotencyRecord.Status.SUCCESS);
            idemRepo.save(record);

            return ResponseEntity.status(201).body(jsonResponse);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepo.save(payment);

            var record = idemRepo.findById(key).get();
            record.setStatus(IdempotencyRecord.Status.FAILED);
            record.setResponse("{\"error\":\"gateway failed\"}");
            idemRepo.save(record);
            
            return ResponseEntity.status(502).body(Map.of("error", "gateway failed"));
        }
        
    }

}