ALTER TABLE users ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT '';

UPDATE users SET email = user_name || '@placeholder.com' WHERE email = '';

ALTER TABLE users ALTER COLUMN email DROP DEFAULT;
