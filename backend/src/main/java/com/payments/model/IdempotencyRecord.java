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

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public enum Status { PENDING, SUCCESS }

    public IdempotencyRecord() {}
    public IdempotencyRecord(String key, Status status) {
        this.idempotencyKey = key;
        this.status = status;
    }
    public IdempotencyRecord(String key, Status status, String response) {
        this.idempotencyKey = key;
        this.status = status;
        this.response = response;
    }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
}