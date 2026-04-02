-- Migration to change avatar_url from VARCHAR(500) to TEXT to support Base64
ALTER TABLE admin.users ALTER COLUMN avatar_url TYPE TEXT;
