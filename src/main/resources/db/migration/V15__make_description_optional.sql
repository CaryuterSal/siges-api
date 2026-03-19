ALTER TABLE space_types ALTER COLUMN description DROP NOT NULL;
ALTER TABLE equipment_types ALTER COLUMN description DROP NOT NULL;
ALTER TABLE space_assets ALTER COLUMN description DROP NOT NULL;
ALTER TABLE reservables ALTER COLUMN description DROP NOT NULL;

-- Also for audit tables if they exist and have the column
ALTER TABLE space_types_aud ALTER COLUMN description DROP NOT NULL;
ALTER TABLE equipment_types_aud ALTER COLUMN description DROP NOT NULL;
ALTER TABLE space_assets_aud ALTER COLUMN description DROP NOT NULL;
ALTER TABLE reservables_aud ALTER COLUMN description DROP NOT NULL;
