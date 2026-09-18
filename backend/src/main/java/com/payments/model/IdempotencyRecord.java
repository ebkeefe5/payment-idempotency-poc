package com.payments.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {
    
    @Id 
    @Column(name = "idempotency_key")
    private String idempotency_key;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotency_key, UUID paymentId, String response) {
        this.idempotency_key = idempotency_key;
        this.paymentId = paymentId;
        this.response = response;
        this.createdAt = OffsetDateTime.now();
    }
    
    public String getIdempotencyKey() { return idempotency_key; }
    public void setIdempotencyKey(String idempotency_key) { this.idempotency_key = idempotency_key; }
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public OffsetDateTime getCreatedAt() { return createdAt; } 
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
}