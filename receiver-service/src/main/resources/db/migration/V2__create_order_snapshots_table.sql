CREATE TABLE IF NOT EXISTS receiver.order_snapshots (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_id UUID NOT NULL UNIQUE,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    snapshot JSONB NOT NULL,
    CONSTRAINT fk_order_snapshots_event
        FOREIGN KEY (event_id)
        REFERENCES receiver.received_events (event_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_order_snapshots_order_id ON receiver.order_snapshots(order_id);
