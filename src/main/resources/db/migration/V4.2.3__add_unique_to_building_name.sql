ALTER TABLE buildings
    ADD CONSTRAINT uc_buildings_name UNIQUE (name);