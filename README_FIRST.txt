AREA COMPLAINT MANAGEMENT SYSTEM
NetBeans + Java Swing + MySQL (XAMPP)

DESIGN
- Practical desktop layout inspired by the supplied reference: left-side navigation, clear page headings, search/filter controls, left form + right records table.
- Navy, teal, white and light-grey colour palette.
- Separate administrator and resident portals.

BEFORE RUNNING
1. Start MySQL in XAMPP.
2. Use the existing database named area_complaint_db. For a new installation use sql/area_complaint_db.sql; for an existing installation back up first and run sql/upgrade_existing_database.sql.
3. Open this folder in NetBeans: File > Open Project. Select the folder containing pom.xml.
4. Wait for Maven dependencies to finish downloading.
5. This ZIP includes a starter db.properties for default XAMPP settings (user root, blank password). If your MySQL root account has a password, update db.password in the project root.
6. Right-click the project > Clean and Build, then Run.

MULTI-WARD ADMINISTRATION (SUPER ADMIN + FIVE WARDS)
- Each resident selects their ward during registration. Existing accounts and complaints are assigned to Ward 01 by the upgrade script, so old data remains available.
- Each ADMIN account belongs to exactly one ward. The admin dashboard, complaint list, complaint details, history, status updates and email notifications are restricted to that ward in the Java service layer and SQL queries.
- Existing residents and ward admins are assigned to Ward 01 by the migration when their ward_id is missing. Existing complaint records retain their data and are linked to their resident ward.
- Super Admin can create ward records and ward administrator accounts from the application dashboard. No Ward Admin can create another admin.

ADMIN FEATURES
- Dashboard counters: total, pending, in-progress and resolved complaints.
- Complaint directory with search and status filter.
- Select a complaint to inspect its details.
- Assign a department and change status.
- Save an update note; status/department changes are recorded in complaint_history.
- View the complaint's update history.
- Reports summary with complaint status counts.

RESIDENT FEATURES
- Resident registration and sign-in.
- Submit a complaint with category, title, location and description.
- View submitted complaints and tracking IDs.

DATABASE
- Uses the existing users, categories, departments, complaints and complaint_history tables, with an additive wards table and ward_id columns.
- Does not drop or recreate the database when the application runs.
- Import SQL only if the database has not already been created.

IMPORTANT
- This is a Java Swing desktop application, not a PHP website. It does not run in a browser at localhost.
- Keep db.properties private if you add a database password.
- The source code is under src/. Maven configuration is in pom.xml.


FEATURE UPGRADE NOTES (photo evidence, email, reopen, password recovery)
1. Existing database: BACK UP first. Do NOT import the fresh SQL over your data. Open phpMyAdmin, select area_complaint_db, SQL tab, and run sql/upgrade_existing_database.sql. This adds ward support and preserves current records.
2. Complaint photo uploads are stored under uploads/; the database stores relative paths. Resolution photo support uses complaints.resolution_image_path.
3. Email requires SMTP. Copy SMTP settings from db.properties.example into your local db.properties. For Gmail, enable 2-Step Verification and create an App Password. Do not use your normal Gmail password.
4. Password reset uses a 6-digit email code that expires after 10 minutes. The password_reset_tokens table is created by the migration.
5. Resident reopen requests are recorded in complaint_history and notify active administrators assigned to that resident’s ward by email when SMTP is configured.
6. If SMTP is not configured, email notifications and password reset emails will not be delivered.
7. Never share db.properties or your SMTP App Password.


LOGIN SCREEN UPDATE (2026-11-17)
-------------------------------
The ui/LoginFrame.java file has been redesigned to match the supplied reference more closely. It uses a two-panel layout, custom vector community illustration, rounded login card, aligned form fields, vector icons, password visibility toggle, and the existing authentication/registration/password-reset actions. The database schema and other application modules are unchanged.


LOGIN ALIGNMENT UPDATE (2026-11-17)
-----------------------------------
The login screen now uses a single centered content column inside the right-side card. The emblem, Welcome back heading, and subtitle are horizontally centered. The email/password labels and fields are left-aligned with each other inside a fixed-width centered form. Forgot password is aligned to the form right edge; action buttons share the same width.

RESIDENT DASHBOARD DESIGN UPDATE
--------------------------------
The Resident Portal has been refreshed with a navy/teal dashboard layout:
- Clear Resident Dashboard header with profile, Change password and Log out actions
- Welcome message and complaint-count summary
- Separate cards for complaint submission and complaint records
- Larger, properly sized issue-description area
- Cleaner photo attachment row and submit action
- Improved complaint table spacing and colour-coded status cells
Existing complaint submission, photo upload, complaint listing, reopen request,
password change and logout actions are retained.


MULTI-WARD UPDATE (2026-11-17)
------------------------------
- Added a wards table and ward_id ownership to users and complaints.
- Resident registration now requires selecting an active ward.
- Each ADMIN account is linked to one ward; ward admins only query and update complaints belonging to their own ward.
- Complaint details, history, status updates and notifications are ward-scoped.
- A one-time Super Admin setup flow is available on the login screen; thereafter only the Super Admin dashboard can create Ward Admin accounts.
- Existing users and complaints are assigned to Ward 01 by the additive upgrade script; no existing complaint rows are deleted.
- Super Admin has area-wide ward and account management only; complaint operations remain assigned to ward administrators.


SUPER ADMIN REPORTING + PHOTO EVIDENCE UPDATE (2026-11-17)
--------------------------------------------------------
- Super Admin dashboard now shows area-wide totals: total, pending, in-progress, resolved and rejected complaints.
- Super Admin can view all complaint report rows across all wards and inspect a selected report, including submitted and resolution photos. This is read-only; ward admins continue to change complaint status within their own ward.
- Resident photo selection now previews the chosen image before submission.
- Photo uploads accept verified JPG/JPEG and PNG files up to 5 MB. Files are stored in the configured uploads directory and the database stores an absolute path so the application can locate the file when launched from NetBeans or another working directory.
- Older relative photo paths remain supported by the photo resolver. Keep the uploads folder when moving or backing up the project; database backups alone do not contain uploaded image files.
- The database schema is unchanged by this update. No SQL re-import is required if the current Super Admin + five wards database is already installed.
- Verification performed here: source inspection, ZIP integrity check and Java source syntax/build attempt. A full Maven build and live MySQL/photo round-trip test must still be run on the user's Mac because Maven and the local XAMPP database are not available in this environment.


LOGIN BRANDING LOGO UPDATE
--------------------------
- The supplied Area Management System logo is bundled at resources/icons/area_management_logo.png.
- LoginFrame loads it from /icons/area_management_logo.png and displays it on a white tile over the navy branding panel for contrast.
- The previous community_illustration.png is retained in the project for reuse; it is no longer the main login branding image.
- No SQL changes are required for this visual update.


NEW: BEFORE / AFTER COMPLAINT PHOTO EVIDENCE
- Residents can attach an optional JPG/PNG photo when submitting a complaint.
- Ward Admins can view the submitted photo and attach an after-resolution photo.
- A complaint cannot be marked RESOLVED until the admin has entered resolution remarks
  and an after-resolution photo is available.
- Residents can select a complaint and choose "View complaint photos" to see the
  original submitted photo and the after-resolution photo.
- Super Admin can view both submitted and after-resolution photos from the area-wide
  complaint report. Ward Admin can also view both photos for complaints in their ward.
- Images are stored as files in the configured uploads directory; MySQL stores only
  their file paths. Keep the uploads directory when backing up or moving the project.

DATABASE UPGRADE
- Existing database: back up your database, then run sql/upgrade_existing_database.sql.
  This script adds the photo path columns only if they are missing and does not delete
  existing complaint or user records.
- Fresh database: import sql/area_complaint_db.sql.
