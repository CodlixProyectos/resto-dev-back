-- V1_3__add_profile_fields_to_users.sql
-- Adds profile management fields to the global users table.

ALTER TABLE users 
ADD COLUMN phone_number VARCHAR(20),
ADD COLUMN avatar_url VARCHAR(500);
