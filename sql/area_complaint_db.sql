-- =====================================================================
--  AREA COMPLAINT MANAGEMENT SYSTEM  -  MySQL 8.0+ schema
--  File: area_complaint_db.sql
--  Run once:  mysql -u root -p < area_complaint_db.sql
--  Safe to re-run: tables use IF NOT EXISTS, seed rows use INSERT IGNORE.
--  NOTE: No administrator account is inserted here. Provision each Ward Admin
--  manually with tools.CreateWardAdmin after creating the ward record.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS area_complaint_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE area_complaint_db;

-- ---------------------------------------------------------------------
-- 1. WARDS (independent administrative boundaries)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wards (
    ward_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    ward_number VARCHAR(20) NOT NULL,
    ward_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ward_id),
    UNIQUE KEY uq_wards_number (ward_number),
    UNIQUE KEY uq_wards_name (ward_name)
) ENGINE=InnoDB;

INSERT IGNORE INTO wards (ward_number, ward_name, description) VALUES
('01','Ward 01','First ward'),
('02','Ward 02','Second ward'),
('03','Ward 03','Third ward'),
('04','Ward 04','Fourth ward'),
('05','Ward 05','Fifth ward');

-- ---------------------------------------------------------------------
-- 2. USERS (each resident/admin belongs to one ward)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    address         VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,              -- BCrypt hash (60 chars)
    role            ENUM('RESIDENT','ADMIN','SUPER_ADMIN') NOT NULL DEFAULT 'RESIDENT',
    ward_id         INT UNSIGNED NULL,
    account_status  ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_users_ward_role (ward_id, role, account_status),
    INDEX idx_users_role_status (role, account_status),
    INDEX idx_users_full_name (full_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. CATEGORIES
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    category_id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_name   VARCHAR(100) NOT NULL,
    PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (category_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. DEPARTMENTS
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    department_id    INT UNSIGNED NOT NULL AUTO_INCREMENT,
    department_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (department_id),
    CONSTRAINT uq_departments_name UNIQUE (department_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. COMPLAINTS
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id    INT UNSIGNED NOT NULL AUTO_INCREMENT,
    tracking_id     VARCHAR(20)  NOT NULL,              -- e.g. ACM-2026-0001
    user_id         INT UNSIGNED NOT NULL,
    ward_id         INT UNSIGNED NOT NULL,
    category_id     INT UNSIGNED NOT NULL,
    department_id   INT UNSIGNED NULL,                  -- NULL until admin assigns
    title           VARCHAR(150) NOT NULL,
    description     TEXT         NOT NULL,
    location        VARCHAR(255) NOT NULL,
    image_path      VARCHAR(500) NULL,                  -- relative path, never binary data
    resolution_image_path VARCHAR(500) NULL,             -- evidence uploaded by ward admin after resolution
    status          ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED')
                                 NOT NULL DEFAULT 'PENDING',
    submitted_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (complaint_id),
    CONSTRAINT uq_complaints_tracking UNIQUE (tracking_id),
    CONSTRAINT fk_complaints_ward FOREIGN KEY (ward_id) REFERENCES wards (ward_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_complaints_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_complaints_user (user_id),
    INDEX idx_complaints_ward_status (ward_id, status, submitted_at),
    INDEX idx_complaints_status (status),
    INDEX idx_complaints_category (category_id),
    INDEX idx_complaints_department (department_id),
    INDEX idx_complaints_submitted (submitted_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. COMPLAINT_HISTORY  (append-only audit trail: never UPDATE/DELETE rows)
--    old_status / old_department_id are NULL on the very first record
--    (created when the complaint is submitted).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaint_history (
    history_id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    complaint_id       INT UNSIGNED NOT NULL,
    updated_by         INT UNSIGNED NOT NULL,
    old_status         ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NULL,
    new_status         ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NOT NULL,
    old_department_id  INT UNSIGNED NULL,
    new_department_id  INT UNSIGNED NULL,
    remarks            TEXT NULL,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    CONSTRAINT fk_history_complaint
        FOREIGN KEY (complaint_id) REFERENCES complaints (complaint_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_history_user
        FOREIGN KEY (updated_by) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_history_old_dept
        FOREIGN KEY (old_department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_history_new_dept
        FOREIGN KEY (new_department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_history_complaint_time (complaint_id, updated_at),
    INDEX idx_history_updated_by (updated_by)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. PASSWORD RESET TOKENS (short-lived, one-time email codes)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    reset_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    token_hash VARCHAR(100) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (reset_id),
    INDEX idx_reset_user_expiry (user_id, expires_at),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- SEED DATA (reference data only - no fake users or complaints)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO departments (department_name) VALUES
    ('Road Maintenance Department'),
    ('Waste Management Department'),
    ('Water Supply Department'),
    ('Drainage Department'),
    ('Electrical Department'),
    ('Community Administration');

INSERT IGNORE INTO categories (category_name) VALUES
    ('Damaged Roads'),
    ('Garbage Collection'),
    ('Water Supply'),
    ('Drainage'),
    ('Streetlights'),
    ('Public Safety'),
    ('Other');

-- ---------------------------------------------------------------------
-- OPTIONAL (recommended): least-privilege account for the application.
-- Uncomment, replace the password, then put these credentials in
-- db.properties instead of using root.
-- ---------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'acms_app'@'localhost' IDENTIFIED BY 'ChangeMe_Strong#123';
-- GRANT SELECT, INSERT, UPDATE ON area_complaint_db.* TO 'acms_app'@'localhost';
-- GRANT DELETE ON area_complaint_db.categories TO 'acms_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ---------------------------------------------------------------------
-- VERIFY after running:
--   SHOW TABLES;                         -- expect 7 tables including wards and password_reset_tokens
--   SELECT ward_id,ward_number,ward_name FROM wards; -- five default wards
--   SELECT * FROM departments;           -- expect 6 rows
--   SELECT * FROM categories;            -- expect 7 rows
-- ---------------------------------------------------------------------
