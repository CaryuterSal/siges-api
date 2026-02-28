CREATE TABLE equipments_aud
(
    rev           INTEGER NOT NULL,
    id            BIGINT  NOT NULL,
    spaces_id     BIGINT,
    inventory_num VARCHAR(255)
);

CREATE TABLE space_types_aud
(
    rev         INTEGER NOT NULL,
    revtype     SMALLINT,
    id          BIGINT  NOT NULL,
    name        VARCHAR(45),
    description VARCHAR(400),
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_space_types_aud PRIMARY KEY (rev, id)
);

CREATE TABLE spaces_aud
(
    rev             INTEGER NOT NULL,
    id              BIGINT  NOT NULL,
    space_types_id  BIGINT,
    book_in_advance NUMERIC(21,0),
    capacity        INTEGER
);

ALTER TABLE equipments_aud
    ADD CONSTRAINT pk_equipments_aud PRIMARY KEY (id, rev);

ALTER TABLE spaces_aud
    ADD CONSTRAINT pk_spaces_aud PRIMARY KEY (id, rev);

ALTER TABLE equipments_aud
    ADD CONSTRAINT FK_EQUIPMENTS_AUD_ON_IDRE FOREIGN KEY (id, rev) REFERENCES reservables_aud (id, rev);

ALTER TABLE spaces_aud
    ADD CONSTRAINT FK_SPACES_AUD_ON_IDRE FOREIGN KEY (id, rev) REFERENCES reservables_aud (id, rev);

ALTER TABLE space_types_aud
    ADD CONSTRAINT FK_SPACE_TYPES_AUD_ON_REV FOREIGN KEY (rev) REFERENCES custom_revision_entity (id);

ALTER TABLE reservables_aud
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE reservables
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE reservables_aud
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;