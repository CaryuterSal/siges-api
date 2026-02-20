CREATE TABLE applicant_aud
(
    rev INTEGER NOT NULL,
    id  BIGINT  NOT NULL,
    CONSTRAINT pk_applicant_aud PRIMARY KEY (rev, id)
);

CREATE TABLE buildings_aud
(
    rev     INTEGER NOT NULL,
    revtype SMALLINT,
    id      BIGINT  NOT NULL,
    name    VARCHAR(45),
    CONSTRAINT pk_buildings_aud PRIMARY KEY (rev, id)
);

CREATE TABLE notes_aud
(
    rev            INTEGER NOT NULL,
    revtype        SMALLINT,
    id             BIGINT  NOT NULL,
    comment        VARCHAR(255),
    reservation_id BIGINT,
    created_date   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_notes_aud PRIMARY KEY (rev, id)
);

CREATE TABLE notifications_aud
(
    rev                    INTEGER NOT NULL,
    revtype                SMALLINT,
    id                     BIGINT  NOT NULL,
    title                  VARCHAR(255),
    body                   VARCHAR(255),
    sent_at                TIMESTAMP WITHOUT TIME ZONE,
    related_reservation_id BIGINT,
    user_id                BIGINT,
    CONSTRAINT pk_notifications_aud PRIMARY KEY (rev, id)
);

CREATE TABLE push_tokens_aud
(
    rev          INTEGER      NOT NULL,
    revtype      SMALLINT,
    token        VARCHAR(255) NOT NULL,
    device_id    VARCHAR(255),
    platform     SMALLINT,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    user_id      BIGINT,
    CONSTRAINT pk_push_tokens_aud PRIMARY KEY (rev, token)
);

CREATE TABLE reservables_aud
(
    rev                INTEGER NOT NULL,
    revtype            SMALLINT,
    id                 BIGINT  NOT NULL,
    status             VARCHAR(15),
    description        VARCHAR(400),
    students_available BOOLEAN,
    buildings_id       BIGINT,
    created_at         TIMESTAMP WITHOUT TIME ZONE,
    created_by         TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_reservables_aud PRIMARY KEY (rev, id)
);

CREATE TABLE reservation_recurrence_aud
(
    rev            INTEGER NOT NULL,
    revtype        SMALLINT,
    id             BIGINT  NOT NULL,
    day_of_week    VARCHAR(255),
    reservation_id BIGINT,
    CONSTRAINT pk_reservation_recurrence_aud PRIMARY KEY (rev, id)
);


ALTER TABLE applicant_aud
    ADD CONSTRAINT FK_APPLICANT_AUD_ON_IDRE FOREIGN KEY (id, rev) REFERENCES app_users_aud (id, rev);

ALTER TABLE buildings_aud
    ADD CONSTRAINT FK_BUILDINGS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE notes_aud
    ADD CONSTRAINT FK_NOTES_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE notifications_aud
    ADD CONSTRAINT FK_NOTIFICATIONS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE push_tokens_aud
    ADD CONSTRAINT FK_PUSH_TOKENS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE reservables_aud
    ADD CONSTRAINT FK_RESERVABLES_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE reservation_recurrence_aud
    ADD CONSTRAINT FK_RESERVATION_RECURRENCE_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);
