package com.wokioki.parcelflow.shipment.domain;

public enum ShipmentStatus {
    CREATED,
    ACCEPTED,
    IN_TRANSIT,
    AT_SORTING_CENTER,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNED,
    CANCELLED
}
