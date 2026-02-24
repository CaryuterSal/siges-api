ALTER TABLE push_tokens
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE notes
    ALTER COLUMN created_date SET NOT NULL;

ALTER TABLE notifications
    ALTER COLUMN sent_at SET NOT NULL;