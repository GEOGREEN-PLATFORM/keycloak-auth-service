CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    number VARCHAR(20),
    birthdate DATE,
    profile_photo_id JSONB,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_DATE NOT NULL
);