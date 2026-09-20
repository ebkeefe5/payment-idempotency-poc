package com.payments.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {
    
    @Id
    @Column(name = "idempotency_key", unique = true, nullable = false) 
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public enum Status { PENDING, SUCCESS, FAILED }

    public IdempotencyRecord() {}
    public IdempotencyRecord(String key, Status status) {
        this.idempotencyKey = key;
        this.status = status;
    }
    public IdempotencyRecord(String key, Status status, UUID paymentId) {
        this.idempotencyKey = key;
        this.status = status;
        this.paymentId = paymentId;
    }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
}