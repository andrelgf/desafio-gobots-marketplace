CREATE SCHEMA IF NOT EXISTS marketplace;

CREATE TABLE IF NOT EXISTS marketplace.orders (
    id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_orders_store ON marketplace.orders(store_code);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON marketplace.orders(created_at);

CREATE TABLE IF NOT EXISTS marketplace.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    quantity INT NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES marketplace.orders (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_items_order ON marketplace.order_items(order_id);

CREATE TABLE IF NOT EXISTS marketplace.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    store_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ,
    payload JSONB NOT NULL,

    CONSTRAINT uk_outbox_event_id UNIQUE (event_id),
    CONSTRAINT fk_outbox_events_order
        FOREIGN KEY (order_id)
        REFERENCES marketplace.orders (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_order ON marketplace.outbox_events(order_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_store ON marketplace.outbox_events(store_code);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_created
    ON marketplace.outbox_events(status, created_at);
