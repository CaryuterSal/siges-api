ALTER TABLE reservation_recurrence
    DROP CONSTRAINT FK_RESERVATION_RECURRENCE_ON_RESERVABLE;

ALTER TABLE reservation_recurrence
    ADD reservation_id BIGINT;

ALTER TABLE reservation_recurrence
    ALTER COLUMN reservation_id SET NOT NULL;

ALTER TABLE reservation_recurrence
    ADD CONSTRAINT FK_RESERVATION_RECURRENCE_ON_RESERVATION FOREIGN KEY (reservation_id) REFERENCES reservations (id);

ALTER TABLE reservation_recurrence
    DROP COLUMN reservable_id;