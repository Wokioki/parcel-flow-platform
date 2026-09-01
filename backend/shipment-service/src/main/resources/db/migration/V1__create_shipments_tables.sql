CREATE TABLE shipments
(
  id                  UUID           NOT NULL,
  tracking_number     VARCHAR(30)    NOT NULL,
  customer_id         UUID           NOT NULL,

  sender_name         VARCHAR(150)   NOT NULL,
  sender_address      VARCHAR(500)   NOT NULL,

  recipient_name      VARCHAR(150)   NOT NULL,
  recipient_address   VARCHAR(500)   NOT NULL,

  weight_kg           NUMERIC(10, 3) NOT NULL,

  status              VARCHAR(30)    NOT NULL,

  created_at          TIMESTAMPTZ    NOT NULL,
  updated_at          TIMESTAMPTZ    NOT NULL,

  CONSTRAINT pk_shipments PRIMARY KEY (id),
  CONSTRAINT uk_shipments_tracking_number UNIQUE (tracking_number),

  CONSTRAINT chk_shipments_status CHECK (
    status IN (
               'CREATED',
               'ACCEPTED',
               'IN_TRANSIT',
               'AT_SORTING_CENTER',
               'OUT_FOR_DELIVERY',
               'DELIVERED',
               'DELIVERY_FAILED',
               'RETURNED',
               'CANCELLED'
      )
    ),

  CONSTRAINT chk_shipments_weight CHECK (weight_kg > 0)
);

CREATE INDEX idx_shipments_customer_id
  ON shipments (customer_id);

CREATE INDEX idx_shipments_status
  ON shipments (status);


CREATE TABLE shipment_status_history
(
  id              UUID        NOT NULL,
  shipment_id     UUID        NOT NULL,
  previous_status VARCHAR(30),
  new_status      VARCHAR(30) NOT NULL,
  changed_by      UUID        NOT NULL,
  changed_at      TIMESTAMPTZ NOT NULL,

  CONSTRAINT pk_shipment_status_history PRIMARY KEY (id),

  CONSTRAINT fk_shipment_status_history_shipment
    FOREIGN KEY (shipment_id)
      REFERENCES shipments (id)
      ON DELETE CASCADE
);

CREATE INDEX idx_shipment_status_history_shipment_id
  ON shipment_status_history (shipment_id);

CREATE INDEX idx_shipment_status_history_changed_at
  ON shipment_status_history (changed_at);
