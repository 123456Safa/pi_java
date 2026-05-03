-- Run this SQL command to update your database for the account lockout feature
ALTER TABLE user ADD COLUMN failed_attempts INT DEFAULT 0;
ALTER TABLE user ADD COLUMN lockout_time TIMESTAMP NULL DEFAULT NULL;
