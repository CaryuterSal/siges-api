ALTER TABLE availabilities
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE availabilities
    ALTER COLUMN updated_at SET NOT NULL;