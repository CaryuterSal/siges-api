ALTER TABLE equipments
    ADD inventory_num VARCHAR(255);

ALTER TABLE equipments
    ADD CONSTRAINT uc_equipments_inventorynum UNIQUE (inventory_num);