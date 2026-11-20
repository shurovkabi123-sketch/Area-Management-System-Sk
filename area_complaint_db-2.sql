
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";



CREATE TABLE `categories` (
  `category_id` int(10) UNSIGNED NOT NULL,
  `category_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



INSERT INTO `categories` (`category_id`, `category_name`) VALUES
(1, 'Damaged Roads'),
(4, 'Drainage'),
(2, 'Garbage Collection'),
(7, 'Other'),
(6, 'Public Safety'),
(5, 'Streetlights'),
(3, 'Water Supply');



CREATE TABLE `complaints` (
  `complaint_id` int(10) UNSIGNED NOT NULL,
  `tracking_id` varchar(20) NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `ward_id` int(10) UNSIGNED NOT NULL,
  `category_id` int(10) UNSIGNED NOT NULL,
  `department_id` int(10) UNSIGNED DEFAULT NULL,
  `title` varchar(150) NOT NULL,
  `description` text NOT NULL,
  `location` varchar(255) NOT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  `resolution_image_path` varchar(500) DEFAULT NULL,
  `status` enum('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



INSERT INTO `complaints` (`complaint_id`, `tracking_id`, `user_id`, `ward_id`, `category_id`, `department_id`, `title`, `description`, `location`, `image_path`, `resolution_image_path`, `status`, `submitted_at`, `updated_at`) VALUES
(1, 'ACM-2026-7555535E', 3, 1, 1, 1, 'Road problem', 'it is dangerous for cars.', 'Banani,Road 16', '/Users/shurovkabir/Downloads/AreaComplaintSystem 12/uploads/cdb6396f-5562-4df5-98a9-9b798fd47dce.png', '/Users/shurovkabir/Downloads/AreaComplaintSystem 13/uploads/0e7f5de9-5dba-47b6-b3a7-e86b2453a7de.png', 'RESOLVED', '2026-11-17 12:42:44', '2026-11-17 13:04:27'),
(2, 'ACM-2026-D5A2B78E', 4, 2, 2, 2, 'not taking garbage from outside the home', 'a big dustbin with full of garbage', 'Banani ,Road 4,house 29', '/Users/shurovkabir/Downloads/AreaComplaintSystem 14/uploads/c7f934ba-619d-4c53-b1d4-069d9ab95fae.png', '/Users/shurovkabir/Desktop/java/AreaComplaintSystem FINAL/uploads/e4bcfaa7-9871-4baf-a6f8-c9bf891af5c1.png', 'RESOLVED', '2026-11-17 13:38:33', '2026-11-17 13:50:20');

-- --------------------------------------------------------



CREATE TABLE `complaint_history` (
  `history_id` int(10) UNSIGNED NOT NULL,
  `complaint_id` int(10) UNSIGNED NOT NULL,
  `updated_by` int(10) UNSIGNED NOT NULL,
  `old_status` enum('PENDING','IN_PROGRESS','RESOLVED','REJECTED') DEFAULT NULL,
  `new_status` enum('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NOT NULL,
  `old_department_id` int(10) UNSIGNED DEFAULT NULL,
  `new_department_id` int(10) UNSIGNED DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



INSERT INTO `complaint_history` (`history_id`, `complaint_id`, `updated_by`, `old_status`, `new_status`, `old_department_id`, `new_department_id`, `remarks`, `updated_at`) VALUES
(2, 1, 2, 'RESOLVED', 'RESOLVED', 1, 1, 'updated', '2026-11-17 13:04:27'),
(3, 2, 6, 'PENDING', 'IN_PROGRESS', NULL, 2, 'will be complete within 24 hour', '2026-11-17 13:39:41'),
(4, 2, 6, 'IN_PROGRESS', 'IN_PROGRESS', 2, 2, '', '2026-11-17 13:48:27'),
(5, 2, 6, 'IN_PROGRESS', 'RESOLVED', 2, 2, 'done', '2026-11-17 13:50:20');


CREATE TABLE `departments` (
  `department_id` int(10) UNSIGNED NOT NULL,
  `department_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



INSERT INTO `departments` (`department_id`, `department_name`) VALUES
(6, 'Community Administration'),
(4, 'Drainage Department'),
(5, 'Electrical Department'),
(1, 'Road Maintenance Department'),
(2, 'Waste Management Department'),
(3, 'Water Supply Department');



CREATE TABLE `password_reset_tokens` (
  `reset_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `token_hash` varchar(100) NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;




CREATE TABLE `users` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `address` varchar(255) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `role` enum('RESIDENT','ADMIN','SUPER_ADMIN') NOT NULL DEFAULT 'RESIDENT',
  `ward_id` int(10) UNSIGNED DEFAULT NULL,
  `account_status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



INSERT INTO `users` (`user_id`, `full_name`, `email`, `phone`, `address`, `password_hash`, `role`, `ward_id`, `account_status`, `created_at`) VALUES
(2, 'MAHE ALAM BAPPY', 'bappy@gmail.com', '01611111111', 'Banani 16', '$2a$12$spmgSHqM7zVc.a3b5EUXpeivNuOl3ipBkezTUVbJBvZSzRsuyG.i6', 'ADMIN', 1, 'ACTIVE', '2026-11-17 12:35:57'),
(3, 'NAVID', 'navid@gmail.com', '01600000000', 'Banani ,Road 16', '$2a$12$MrOfLwmlps678xULURZPSO9WKK2El6WY/2d5PQnCyYawyeY2nplni', 'RESIDENT', 1, 'ACTIVE', '2026-11-17 12:37:33'),
(4, 'sakib', 'sakib@gmail.com', '01600000002', 'Banani,Road 4', '$2a$12$Dds51PoB4Z.Soo0HFCjy5.2LB8Z2wWUdye1IZKUQW82e/p0Hgcrq2', 'RESIDENT', 2, 'ACTIVE', '2026-11-17 13:30:49'),
(5, 'AREA SUPER ADMIN', 'superadmin@gmail.com', '01600000001', 'System Administration', '$2a$12$2HYCvGT./g5XnVf0KcG/IuRJKTL3Won5OYYI.ycnM2Kc6j2fjFLhO', 'SUPER_ADMIN', NULL, 'ACTIVE', '2026-11-17 13:33:26'),
(6, 'SHUROV KABIR', 'shurov@gmail.com', '01601818867', 'Banani,Road4', '$2a$12$M5pR/jvsIsSvjhC/2RqZZOtYhjaC8E0WAjESROtEaQqM4dv.Awgwe', 'ADMIN', 2, 'ACTIVE', '2026-11-17 13:36:00');



CREATE TABLE `wards` (
  `ward_id` int(10) UNSIGNED NOT NULL,
  `ward_number` varchar(20) NOT NULL,
  `ward_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--

INSERT INTO `wards` (`ward_id`, `ward_number`, `ward_name`, `description`, `active`, `created_at`) VALUES
(1, '01', 'Ward 01', 'First ward', 1, '2026-11-17 11:35:15'),
(2, '02', 'Ward 02', 'Second ward', 1, '2026-11-17 11:35:15'),
(3, '03', 'Ward 03', 'Third ward', 1, '2026-11-17 11:35:15'),
(4, '04', 'Ward 04', 'Fourth ward', 1, '2026-11-17 11:35:15'),
(5, '05', 'Ward 05', 'Fifth ward', 1, '2026-11-17 11:35:15');


ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD UNIQUE KEY `uq_categories_name` (`category_name`);


ALTER TABLE `complaints`
  ADD PRIMARY KEY (`complaint_id`),
  ADD UNIQUE KEY `uq_complaints_tracking` (`tracking_id`),
  ADD KEY `idx_complaints_user` (`user_id`),
  ADD KEY `idx_complaints_ward_status` (`ward_id`,`status`,`submitted_at`),
  ADD KEY `idx_complaints_status` (`status`),
  ADD KEY `idx_complaints_category` (`category_id`),
  ADD KEY `idx_complaints_department` (`department_id`),
  ADD KEY `idx_complaints_submitted` (`submitted_at`);


ALTER TABLE `complaint_history`
  ADD PRIMARY KEY (`history_id`),
  ADD KEY `fk_history_old_dept` (`old_department_id`),
  ADD KEY `fk_history_new_dept` (`new_department_id`),
  ADD KEY `idx_history_complaint_time` (`complaint_id`,`updated_at`),
  ADD KEY `idx_history_updated_by` (`updated_by`);


ALTER TABLE `departments`
  ADD PRIMARY KEY (`department_id`),
  ADD UNIQUE KEY `uq_departments_name` (`department_name`);


ALTER TABLE `password_reset_tokens`
  ADD PRIMARY KEY (`reset_id`),
  ADD KEY `idx_reset_user_expiry` (`user_id`,`expires_at`);


ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uq_users_email` (`email`),
  ADD KEY `idx_users_ward_role` (`ward_id`,`role`,`account_status`),
  ADD KEY `idx_users_role_status` (`role`,`account_status`),
  ADD KEY `idx_users_full_name` (`full_name`);


ALTER TABLE `wards`
  ADD PRIMARY KEY (`ward_id`),
  ADD UNIQUE KEY `uq_wards_number` (`ward_number`),
  ADD UNIQUE KEY `uq_wards_name` (`ward_name`);


ALTER TABLE `categories`
  MODIFY `category_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;


ALTER TABLE `complaints`
  MODIFY `complaint_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;


ALTER TABLE `complaint_history`
  MODIFY `history_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;


ALTER TABLE `departments`
  MODIFY `department_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;


ALTER TABLE `password_reset_tokens`
  MODIFY `reset_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;


ALTER TABLE `users`
  MODIFY `user_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;


ALTER TABLE `wards`
  MODIFY `ward_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;


ALTER TABLE `complaints`
  ADD CONSTRAINT `fk_complaints_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_complaints_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_complaints_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_complaints_ward` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`) ON UPDATE CASCADE;


ALTER TABLE `complaint_history`
  ADD CONSTRAINT `fk_history_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`complaint_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_history_new_dept` FOREIGN KEY (`new_department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_history_old_dept` FOREIGN KEY (`old_department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_history_user` FOREIGN KEY (`updated_by`) REFERENCES `users` (`user_id`) ON UPDATE CASCADE;


ALTER TABLE `password_reset_tokens`
  ADD CONSTRAINT `fk_reset_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_ward` FOREIGN KEY (`ward_id`) REFERENCES `wards` (`ward_id`) ON UPDATE CASCADE;
COMMIT;

