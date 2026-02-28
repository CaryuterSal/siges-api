ALTER TABLE availability_exceptions
ADD COLUMN reason VARCHAR(255) default 'no reason' NOT NULL;