-- ACMS MULTI-WARD + SUPER ADMIN UPGRADE (MySQL 8+)
-- BACK UP area_complaint_db before running. Does not drop tables or records.
USE area_complaint_db;

CREATE TABLE IF NOT EXISTS wards (
  ward_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  ward_number VARCHAR(20) NOT NULL,
  ward_name VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ward_id), UNIQUE KEY uq_wards_number (ward_number), UNIQUE KEY uq_wards_name (ward_name)
) ENGINE=InnoDB;

INSERT IGNORE INTO wards(ward_number,ward_name,description) VALUES
('01','Ward 01','First ward'),('02','Ward 02','Second ward'),('03','Ward 03','Third ward'),('04','Ward 04','Fourth ward'),('05','Ward 05','Fifth ward');

-- Existing users/complaints are kept. Assign legacy NULL ward references to Ward 01.
ALTER TABLE users MODIFY ward_id INT UNSIGNED NULL;
ALTER TABLE users MODIFY role ENUM('RESIDENT','ADMIN','SUPER_ADMIN') NOT NULL DEFAULT 'RESIDENT';
UPDATE users SET ward_id=(SELECT ward_id FROM wards WHERE ward_number='01' LIMIT 1)
 WHERE ward_id IS NULL AND role IN ('RESIDENT','ADMIN');

-- Ensure complaint ward_id exists and preserve existing data.
SET @has_complaint_ward = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='complaints' AND COLUMN_NAME='ward_id');
SET @sql = IF(@has_complaint_ward=0,
 'ALTER TABLE complaints ADD COLUMN ward_id INT UNSIGNED NULL AFTER user_id',
 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
UPDATE complaints c JOIN users u ON u.user_id=c.user_id SET c.ward_id=u.ward_id WHERE c.ward_id IS NULL;
UPDATE complaints SET ward_id=(SELECT ward_id FROM wards WHERE ward_number='01' LIMIT 1) WHERE ward_id IS NULL;
ALTER TABLE complaints MODIFY ward_id INT UNSIGNED NOT NULL;

-- Add foreign keys only when absent.
SET @fk_users = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME='fk_users_ward');
SET @sql = IF(@fk_users=0,'ALTER TABLE users ADD CONSTRAINT fk_users_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id) ON UPDATE CASCADE ON DELETE RESTRICT','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk_complaints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND CONSTRAINT_NAME='fk_complaints_ward');
SET @sql = IF(@fk_complaints=0,'ALTER TABLE complaints ADD CONSTRAINT fk_complaints_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id) ON UPDATE CASCADE ON DELETE RESTRICT','SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- Complaint evidence support: keep existing rows and add nullable path columns only if missing.
SET @has_complaint_image = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='complaints' AND COLUMN_NAME='image_path');
SET @sql = IF(@has_complaint_image=0,
  'ALTER TABLE complaints ADD COLUMN image_path VARCHAR(500) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_resolution_image = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='complaints' AND COLUMN_NAME='resolution_image_path');
SET @sql = IF(@has_resolution_image=0,
  'ALTER TABLE complaints ADD COLUMN resolution_image_path VARCHAR(500) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT ward_id,ward_number,ward_name FROM wards ORDER BY ward_number;
SELECT user_id,full_name,email,role,ward_id FROM users ORDER BY user_id;
