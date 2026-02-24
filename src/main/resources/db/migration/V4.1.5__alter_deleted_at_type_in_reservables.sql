ALTER TABLE reservables
    DROP COLUMN deleted_at;

ALTER TABLE reservables
    ADD COLUMN deleted_at timestamp with time zone;



