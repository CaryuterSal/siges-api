ALTER TABLE spaces
    ADD book_in_advance NUMERIC(21,0);

ALTER TABLE spaces
    ALTER COLUMN book_in_advance SET NOT NULL;


ALTER TABLE spaces
    ADD capacity INT DEFAULT 1 NOT NULL;

ALTER TABLE buildings_aud
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;