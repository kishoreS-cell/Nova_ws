-- ============================================================
-- LeaseApp Database Schema
-- Nova Scotia Form P (Standard Form of Lease) - Digitized
-- ============================================================

CREATE DATABASE IF NOT EXISTS leaseapp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE leaseapp;

-- ============================================================
-- 1. USERS TABLE (Homeowner login only - tenants never log in)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. LEASES TABLE (Homeowner-filled sections + shared lease-level fields)
-- ============================================================
CREATE TABLE IF NOT EXISTS leases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    homeowner_id INT NOT NULL,

    -- Section 1 (Landlord part)
    landlord_name VARCHAR(150),
    landlord_street_address VARCHAR(200),
    landlord_mailing_address VARCHAR(200),
    landlord_apartment VARCHAR(20),
    landlord_city VARCHAR(100),
    landlord_postal_code VARCHAR(15),
    landlord_phone_home VARCHAR(20),
    landlord_phone_business VARCHAR(20),

    -- Section 3 (Premises)
    premises_street_address VARCHAR(200),
    premises_apartment VARCHAR(20),
    premises_city VARCHAR(100),
    premises_postal_code VARCHAR(15),
    property_type VARCHAR(100),
    tenant_mailing_pobox VARCHAR(100),
    tenant_mailing_postal_code VARCHAR(15),
    tenant_phone_home VARCHAR(20),
    tenant_phone_business VARCHAR(20),

    -- Section 4 (Emergency contact) - filled by tenant via secure link, but stored
    -- at the lease level since the form has one emergency contact per application
    emergency_next_of_kin VARCHAR(150),
    emergency_phone_work VARCHAR(20),
    emergency_phone_home VARCHAR(20),

    -- Section 9 (Landlord's email for tenant to serve documents)
    landlord_email_for_service VARCHAR(150),

    -- Section 11A / 11B (Lease type)
    lease_type ENUM('PERIODIC','FIXED_TERM'),
    periodic_start_date DATE,
    periodic_term ENUM('YEAR_TO_YEAR','MONTH_TO_MONTH','WEEK_TO_WEEK'),
    fixed_start_date DATE,
    fixed_end_date DATE,

    -- Section 13 (Rent)
    rent_amount DECIMAL(10,2),
    rent_frequency ENUM('WEEK','MONTH'),
    payment_method ENUM('CASH','AUTO_WITHDRAWAL','POST_DATED_CHEQUES','CHEQUE','OTHER'),
    payment_method_other VARCHAR(100),
    rent_due_day INT,
    rent_payable_to VARCHAR(150),

    -- Section 16 (Rent includes - Appliances)
    incl_stove BOOLEAN DEFAULT FALSE,
    incl_fridge BOOLEAN DEFAULT FALSE,
    incl_washer_dryer BOOLEAN DEFAULT FALSE,
    incl_dishwasher BOOLEAN DEFAULT FALSE,
    incl_furniture BOOLEAN DEFAULT FALSE,

    -- Section 16 (Utilities)
    incl_coin_laundry BOOLEAN DEFAULT FALSE,
    incl_cable BOOLEAN DEFAULT FALSE,
    incl_heat BOOLEAN DEFAULT FALSE,
    incl_water BOOLEAN DEFAULT FALSE,
    incl_hot_water BOOLEAN DEFAULT FALSE,
    incl_electricity BOOLEAN DEFAULT FALSE,
    incl_parking_spaces INT DEFAULT 0,
    incl_parking_space_number VARCHAR(20),
    incl_recycling_facilities BOOLEAN DEFAULT FALSE,

    -- Section 16 (Other included)
    incl_lawn_care BOOLEAN DEFAULT FALSE,
    incl_snow_removal BOOLEAN DEFAULT FALSE,
    incl_garbage_removal BOOLEAN DEFAULT FALSE,
    incl_other_custom VARCHAR(200),

    -- Section 16 (Tenant responsibilities)
    resp_lawn_care BOOLEAN DEFAULT FALSE,
    resp_late_payment BOOLEAN DEFAULT FALSE,
    resp_snow_removal BOOLEAN DEFAULT FALSE,
    resp_returned_cheque_limit DECIMAL(10,2),
    resp_garbage_removal BOOLEAN DEFAULT FALSE,
    resp_parking_fee DECIMAL(10,2),
    resp_parking_spaces INT,
    resp_tenant_insurance BOOLEAN DEFAULT FALSE,
    resp_lockout_charge_limit DECIMAL(10,2),
    resp_separation_recyclables BOOLEAN DEFAULT FALSE,
    resp_sublet_expense_limit DECIMAL(10,2) DEFAULT 75.00,

    -- Section 17 (Additional obligations)
    additional_obligations TEXT,

    -- Section 18 (Security deposit)
    security_deposit_required BOOLEAN DEFAULT FALSE,
    security_deposit_amount DECIMAL(10,2),
    security_deposit_institution VARCHAR(150),

    -- Section 19 (Inspection)
    inspection_report_attached BOOLEAN DEFAULT FALSE,

    -- Section 20 (Statutory conditions / building rules)
    rules_attached BOOLEAN DEFAULT FALSE,
    rules_schedule_ref VARCHAR(50),

    -- Status tracking (state machine)
    status ENUM('DRAFT','PENDING_TENANT_SIGNATURES','PENDING_HOMEOWNER_SIGNATURE','COMPLETED')
        DEFAULT 'DRAFT',

    homeowner_signature_data LONGTEXT,
    homeowner_signed_date DATE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_lease_homeowner FOREIGN KEY (homeowner_id) REFERENCES users(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 3. TENANTS TABLE (Section 1 remaining, 8, 26, signature)
-- ============================================================
CREATE TABLE IF NOT EXISTS tenants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    lease_id INT NOT NULL,
    tenant_order INT NOT NULL,           -- 1, 2, or 3

    -- Section 1 (Tenant name)
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,

    -- Section 8 (Email address for service by landlord on tenant)
    email_for_service VARCHAR(150),

    -- Section 26 (Attachments acknowledgment - initials)
    ack_received_act BOOLEAN DEFAULT FALSE,
    ack_received_signed_copy BOOLEAN DEFAULT FALSE,
    ack_received_rules BOOLEAN DEFAULT FALSE,

    -- Secure link
    secure_token VARCHAR(255) UNIQUE,
    token_expiry TIMESTAMP NULL,

    -- Signature
    is_signed BOOLEAN DEFAULT FALSE,
    signature_data LONGTEXT,
    signed_date DATE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_lease FOREIGN KEY (lease_id) REFERENCES leases(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_lease_tenant_order UNIQUE (lease_id, tenant_order)
);


-- ============================================================
-- 4. OCCUPANTS TABLE (Section 2 - under 18 / non-signing occupants)
-- ============================================================
CREATE TABLE IF NOT EXISTS occupants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    lease_id INT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    age INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_occupant_lease FOREIGN KEY (lease_id) REFERENCES leases(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 5. VIEW: Lease summary with homeowner + signing progress
-- ============================================================
CREATE OR REPLACE VIEW view_lease_summary AS
SELECT
    l.id AS lease_id,
    l.homeowner_id,
    u.full_name AS homeowner_name,
    u.email AS homeowner_email,
    l.premises_street_address,
    l.premises_city,
    l.rent_amount,
    l.status,
    COUNT(t.id) AS total_tenants,
    SUM(CASE WHEN t.is_signed = TRUE THEN 1 ELSE 0 END) AS tenants_signed,
    l.homeowner_signature_data IS NOT NULL AS homeowner_has_signed,
    l.created_at,
    l.updated_at
FROM leases l
JOIN users u ON u.id = l.homeowner_id
LEFT JOIN tenants t ON t.lease_id = l.id
GROUP BY l.id;


-- ============================================================
-- 6. VIEW: Pending tenant actions (for reminder emails / cron jobs)
-- ============================================================
CREATE OR REPLACE VIEW view_pending_tenant_signatures AS
SELECT
    t.id AS tenant_id,
    t.lease_id,
    t.full_name AS tenant_name,
    t.email,
    t.secure_token,
    t.token_expiry,
    l.status AS lease_status,
    l.premises_street_address
FROM tenants t
JOIN leases l ON l.id = t.lease_id
WHERE t.is_signed = FALSE
  AND l.status = 'PENDING_TENANT_SIGNATURES';


-- ============================================================
-- 7. Helpful indexes (guarded - safe to re-run)
-- ============================================================
SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = 'leaseapp' AND table_name = 'tenants' AND index_name = 'idx_tenants_token'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_tenants_token ON tenants(secure_token)',
    'SELECT "idx_tenants_token already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = 'leaseapp' AND table_name = 'leases' AND index_name = 'idx_leases_homeowner'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_leases_homeowner ON leases(homeowner_id)',
    'SELECT "idx_leases_homeowner already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = 'leaseapp' AND table_name = 'leases' AND index_name = 'idx_leases_status'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX idx_leases_status ON leases(status)',
    'SELECT "idx_leases_status already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- End of script
-- ============================================================


select * from leases;