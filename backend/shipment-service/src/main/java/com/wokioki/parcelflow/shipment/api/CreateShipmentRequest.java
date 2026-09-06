package com.wokioki.parcelflow.shipment.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShipmentRequest(

    @NotBlank
    @Size(max = 150)
    String senderName,

    @NotBlank
    @Size(max = 500)
    String senderAddress,

    @NotBlank
    @Size(max = 150)
    String recipientName,

    @NotBlank
    @Size(max = 500)
    String recipientAddress,

    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 7, fraction = 3)
    BigDecimal weightKg
) {
}
