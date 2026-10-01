ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255),
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

ALTER TABLE users
    ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'));
