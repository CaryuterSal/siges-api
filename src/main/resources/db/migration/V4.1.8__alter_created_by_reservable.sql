ALTER TABLE reservables
    DROP COLUMN created_by;

ALTER TABLE reservables
    ADD created_by VARCHAR(255) NOT NULL;

ALTER TABLE reservables_aud
    DROP COLUMN created_by;

ALTER TABLE reservables_aud
    ADD created_by VARCHAR(255);