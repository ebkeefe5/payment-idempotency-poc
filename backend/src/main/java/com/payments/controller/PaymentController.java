package com.payments.controller;

import com.payments.dto.PaymentRequest;
import com.payments.model.Payment;
import com.payments.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/payments")
    public ResponseEntity<Payment> pay(@RequestHeader("Idempotency-Key") String key, @Valid @RequestBody PaymentRequest request) {
        return paymentService.process(key, request.amount());
    }
}