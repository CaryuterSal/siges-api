ALTER TABLE reservations
    ADD approval_observation VARCHAR(255);

ALTER TABLE reservations
    ADD rejection_reason VARCHAR(255);

ALTER TABLE reservations
    ADD request_reason VARCHAR(255);

ALTER TABLE reservations_aud
    ADD approval_observation VARCHAR(255);

ALTER TABLE reservations_aud
    ADD rejection_reason VARCHAR(255);

ALTER TABLE reservations_aud
    ADD request_reason VARCHAR(255);