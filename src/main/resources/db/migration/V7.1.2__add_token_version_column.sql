ALTER TABLE app_users
    ADD token_version INTEGER;

UPDATE app_users
SET token_version = 0
WHERE token_version IS NULL;
ALTER TABLE app_users
    ALTER COLUMN token_version SET NOT NULL;

ALTER TABLE app_users_aud
    ADD token_version INTEGER;