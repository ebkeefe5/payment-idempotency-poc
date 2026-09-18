package com.payments.service;

import com.payments.model.IdempotencyRecord;
import com.payments.model.Payment;
import com.payments.repository.IdempotencyRepository;
import com.payments.repository.PaymentRepository;
import java.math.BigDecimal;
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

    @Transactional 
    public Payment process(String key, BigDecimal amount) {
        // 1. Return old payment if key seen
        var existing = idemRepo.findById(key);
        if (existing.isPresent()) {
            return paymentRepo.findById(existing.get().getPaymentId()).orElseThrow();
        }

        //2. Else create new payment 
        Payment payment = new Payment();

        payment.setAmount(amount);
        paymentRepo.save(payment);

        //3. save proof
        IdempotencyRecord record = new IdempotencyRecord(
            key,
            payment.getId(),
            "{\"id\":\""+ payment.getId() + "\"}"
        );
        idemRepo.save(record);

        return payment;
    }

}