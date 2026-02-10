ALTER TABLE app_users
    DROP COLUMN deleted_at;

ALTER TABLE app_users
    ADD COLUMN deleted_at TIMESTAMPTZ;