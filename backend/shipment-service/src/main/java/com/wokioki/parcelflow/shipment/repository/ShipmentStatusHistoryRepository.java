package com.wokioki.parcelflow.shipment.repository;

import com.wokioki.parcelflow.shipment.domain.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentStatusHistoryRepository
    extends JpaRepository<ShipmentStatusHistory, UUID> {

    List<ShipmentStatusHistory> findAllByShipmentIdOrderByChangedAtAsc(UUID shipmentId);
}
