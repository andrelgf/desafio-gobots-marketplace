CREATE SCHEMA IF NOT EXISTS receiver;

CREATE TABLE IF NOT EXISTS receiver.subscriptions (
    id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_subscriptions_store_code UNIQUE (store_code)
);

CREATE TABLE IF NOT EXISTS receiver.received_events (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    store_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload JSONB NOT NULL,
    CONSTRAINT uk_received_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_received_events_store ON receiver.received_events(store_code);
CREATE INDEX IF NOT EXISTS idx_received_events_order ON receiver.received_events(order_id);
