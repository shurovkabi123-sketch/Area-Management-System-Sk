-- Example only: replace the ward number/name with your actual ward details.
USE area_complaint_db;
INSERT INTO wards (ward_number, ward_name, description)
VALUES ('02', 'Ward 02', 'Enter a short description for this ward');

-- Find the generated ID. Use this ward_id when creating its administrator.
SELECT ward_id, ward_number, ward_name FROM wards ORDER BY ward_number;
