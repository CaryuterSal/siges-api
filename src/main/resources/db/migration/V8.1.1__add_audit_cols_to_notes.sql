ALTER TABLE notes
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE notes
    ADD created_by VARCHAR(255);

ALTER TABLE notes
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE notes
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE notes_aud
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE notes_aud
    ADD created_by VARCHAR(255);

ALTER TABLE notes_aud
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE notes
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE notes
    DROP COLUMN created_date;