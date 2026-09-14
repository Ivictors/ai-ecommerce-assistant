CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(150) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          description TEXT NOT NULL,
                          price NUMERIC(19, 2) NOT NULL,
                          stock INTEGER NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_products_price
                              CHECK (price >= 0),

                          CONSTRAINT chk_products_stock
                              CHECK (stock >= 0)
);

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        total NUMERIC(19, 2) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_orders_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id),

                        CONSTRAINT chk_orders_status
                            CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),

                        CONSTRAINT chk_orders_total
                            CHECK (total >= 0)
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(19, 2) NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id),

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id),

                             CONSTRAINT chk_order_items_quantity
                                 CHECK (quantity > 0),

                             CONSTRAINT chk_order_items_unit_price
                                 CHECK (unit_price >= 0)
);

CREATE TABLE conversations (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_conversations_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);

CREATE INDEX idx_conversations_user_id
    ON conversations(user_id);