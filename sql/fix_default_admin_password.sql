-- Run this only if you imported the earlier full SQL containing admin@acms.local.
-- It does not delete data or recreate tables.
USE area_complaint_db;
UPDATE users
SET password_hash = '$2a$12$3eXTTvfJoacpMhngNKJl7uXQgWtWKOlFAeg7mosPk3H25EdypN502', account_status = 'ACTIVE'
WHERE email = 'admin@acms.local' AND role = 'ADMIN';
-- Login: admin@acms.local / Admin@12345
