ALTER TABLE push_tokens_aud
    ADD COLUMN is_active BOOLEAN;

ALTER TABLE push_tokens
    ALTER COLUMN is_active SET NOT NULL;

ALTER TABLE push_tokens
    ALTER COLUMN is_active SET DEFAULT TRUE;