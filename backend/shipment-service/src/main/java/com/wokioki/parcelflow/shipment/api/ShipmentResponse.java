package com.wokioki.parcelflow.shipment.api;

import com.wokioki.parcelflow.shipment.domain.Shipment;
import com.wokioki.parcelflow.shipment.domain.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
    UUID id,
    String trackingNumber,
    UUID customerId,
    String senderName,
    String senderAddress,
    String recipientName,
    String recipientAddress,
    BigDecimal weightKg,
    ShipmentStatus status,
    Instant createdAt,
    Instant updatedAt
) {

    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
            shipment.getId(),
            shipment.getTrackingNumber(),
            shipment.getCustomerId(),
            shipment.getSenderName(),
            shipment.getSenderAddress(),
            shipment.getRecipientName(),
            shipment.getRecipientAddress(),
            shipment.getWeightKg(),
            shipment.getStatus(),
            shipment.getCreatedAt(),
            shipment.getUpdatedAt()
        );
    }
}
