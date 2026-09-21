package com.payments.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record PaymentRequest(
    @NotNull @Positive BigDecimal amount
) {}