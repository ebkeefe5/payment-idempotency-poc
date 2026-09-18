package com.payments.dto;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {}