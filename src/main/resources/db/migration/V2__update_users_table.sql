ALTER TABLE users
    ADD COLUMN creation_date_tz TIMESTAMPTZ,
    ADD COLUMN update_date TIMESTAMPTZ;

UPDATE users
    SET creation_date_tz = creation_date;

UPDATE users
    SET update_date = creation_date;

ALTER TABLE users
    DROP COLUMN creation_date,
    ALTER COLUMN creation_date_tz SET NOT NULL,
    ALTER COLUMN update_date SET NOT NULL;

ALTER TABLE users
    RENAME COLUMN creation_date_tz TO creation_date;