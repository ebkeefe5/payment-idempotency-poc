package com.payments.service;

import com.payments.model.IdempotencyRecord;
import com.payments.model.Payment;
import com.payments.repository.IdempotencyRepository;
import com.payments.repository.PaymentRepository;
import java.math.BigDecimal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final IdempotencyRepository idemRepo;

    public PaymentService(PaymentRepository paymentRepo, IdempotencyRepository idemRepo) {
        this.paymentRepo = paymentRepo;
        this.idemRepo = idemRepo;
    }

    public ResponseEntity process(String key, BigDecimal amount) {
        // 1. Check if key already exists
        var existingOpt = idemRepo.findByIdempotencyKey(key);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getStatus() == IdempotencyRecord.Status.PENDING) {
                return ResponseEntity.status(409).body("{\"error\"already processing\"}"); 
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
                    return ResponseEntity.status(409).body("{\"error\"already processing\"}"); 
                } else {
                    return ResponseEntity.ok(existing2.getResponse());
                }
            }
        }

        try {
            Thread.sleep(2000); // uncomment to test race condition for 409
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        

        // 3. We own the key, create payment
        Payment payment = new Payment();

        payment.setAmount(amount);
        paymentRepo.save(payment);

        String jsonResponse = "{\"id\":\""+ payment.getId() + "\"}";

        //4. Mark as SUCCESS
        var record = idemRepo.findById(key).get();

        record.setResponse(jsonResponse);

        record.setStatus(IdempotencyRecord.Status.SUCCESS);
        idemRepo.save(record);

        return ResponseEntity.status(201).body(jsonResponse);
    }

}