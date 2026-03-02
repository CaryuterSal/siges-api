ALTER TABLE app_users
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE app_users
SET updated_at = created_at
WHERE updated_at IS NULL;
ALTER TABLE app_users
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE app_users_aud
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;