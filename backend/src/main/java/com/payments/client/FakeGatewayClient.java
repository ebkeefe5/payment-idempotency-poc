package com.payments.client;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeGatewayClient {

    public GatewayResult charge(BigDecimal amount) throws InterruptedException {
        
        try {
            Thread.sleep(2000); // replace with real gateway HTTP call 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }

        return new GatewayResult("SUCCESS", UUID.randomUUID().toString());

    }

    public record GatewayResult(String status, String gatewayId) {}

}