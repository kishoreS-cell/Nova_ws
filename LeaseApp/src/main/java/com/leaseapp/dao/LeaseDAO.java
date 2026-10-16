package com.leaseapp.dao;

import com.leaseapp.model.Lease;
import com.leaseapp.model.homeowner.*;
import com.leaseapp.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaseDAO {

    // Section 4 (Emergency contact) is filled by the tenant via the secure link,
    // but stored at the lease level since the form has one emergency contact per application
    public boolean updateEmergencyContact(int leaseId, com.leaseapp.model.tenant.EmergencyContact ec) throws SQLException {
        String sql = "UPDATE leases SET emergency_next_of_kin = ?, emergency_phone_work = ?, emergency_phone_home = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ec.getNextOfKin());
            ps.setString(2, ec.getPhoneWork());
            ps.setString(3, ec.getPhoneHome());
            ps.setInt(4, leaseId);
            return ps.executeUpdate() > 0;
        }
    }

    // Create a new lease (homeowner's initial submission) - starts in DRAFT status
    public int createLease(Lease lease) throws SQLException {
        LandlordInfo l = lease.getLandlordInfo();
        PremisesInfo p = lease.getPremisesInfo();
        LandlordServiceEmail lse = lease.getLandlordServiceEmail();
        LeaseTerm lt = lease.getLeaseTerm();
        RentDetails rd = lease.getRentDetails();
        RentIncludes ri = lease.getRentIncludes();
        TenantResponsibilities tr = lease.getTenantResponsibilities();
        SecurityDeposit sd = lease.getSecurityDeposit();

        String sql = "INSERT INTO leases (" +
                "homeowner_id, landlord_name, landlord_street_address, landlord_mailing_address, " +
                "landlord_apartment, landlord_city, landlord_postal_code, landlord_phone_home, landlord_phone_business, " +
                "premises_street_address, premises_apartment, premises_city, premises_postal_code, property_type, " +
                "tenant_mailing_pobox, tenant_mailing_postal_code, tenant_phone_home, tenant_phone_business, " +
                "landlord_email_for_service, " +
                "lease_type, periodic_start_date, periodic_term, fixed_start_date, fixed_end_date, " +
                "rent_amount, rent_frequency, payment_method, payment_method_other, rent_due_day, rent_payable_to, " +
                "incl_stove, incl_fridge, incl_washer_dryer, incl_dishwasher, incl_furniture, " +
                "incl_coin_laundry, incl_cable, incl_heat, incl_water, incl_hot_water, incl_electricity, " +
                "incl_parking_spaces, incl_parking_space_number, incl_recycling_facilities, " +
                "incl_lawn_care, incl_snow_removal, incl_garbage_removal, incl_other_custom, " +
                "resp_lawn_care, resp_late_payment, resp_snow_removal, resp_returned_cheque_limit, " +
                "resp_garbage_removal, resp_parking_fee, resp_parking_spaces, resp_tenant_insurance, " +
                "resp_lockout_charge_limit, resp_separation_recyclables, resp_sublet_expense_limit, " +
                "additional_obligations, " +
                "security_deposit_required, security_deposit_amount, security_deposit_institution, " +
                "inspection_report_attached, rules_attached, rules_schedule_ref, status" +
                ") VALUES (" +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, " +
                "?, " +
                "?, ?, ?, " +
                "?, ?, ?, ?" +
                ")";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setInt(i++, lease.getHomeownerId());
            ps.setString(i++, l.getName());
            ps.setString(i++, l.getStreetAddress());
            ps.setString(i++, l.getMailingAddress());
            ps.setString(i++, l.getApartment());
            ps.setString(i++, l.getCity());
            ps.setString(i++, l.getPostalCode());
            ps.setString(i++, l.getPhoneHome());
            ps.setString(i++, l.getPhoneBusiness());

            ps.setString(i++, p.getStreetAddress());
            ps.setString(i++, p.getApartment());
            ps.setString(i++, p.getCity());
            ps.setString(i++, p.getPostalCode());
            ps.setString(i++, p.getPropertyType());

            ps.setString(i++, p.getTenantMailingPobox());
            ps.setString(i++, p.getTenantMailingPostalCode());
            ps.setString(i++, p.getTenantPhoneHome());
            ps.setString(i++, p.getTenantPhoneBusiness());

            ps.setString(i++, lse.getEmailAddress());

            ps.setString(i++, lt.getLeaseType());
            ps.setDate(i++, lt.getPeriodicStartDate());
            ps.setString(i++, lt.getPeriodicTerm());
            ps.setDate(i++, lt.getFixedStartDate());
            ps.setDate(i++, lt.getFixedEndDate());

            ps.setBigDecimal(i++, rd.getAmount());
            ps.setString(i++, rd.getFrequency());
            ps.setString(i++, rd.getPaymentMethod());
            ps.setString(i++, rd.getPaymentMethodOther());
            ps.setInt(i++, rd.getDueDay());
            ps.setString(i++, rd.getPayableTo());

            ps.setBoolean(i++, ri.isStove());
            ps.setBoolean(i++, ri.isFridge());
            ps.setBoolean(i++, ri.isWasherDryer());
            ps.setBoolean(i++, ri.isDishwasher());
            ps.setBoolean(i++, ri.isFurniture());

            ps.setBoolean(i++, ri.isCoinLaundry());
            ps.setBoolean(i++, ri.isCable());
            ps.setBoolean(i++, ri.isHeat());
            ps.setBoolean(i++, ri.isWater());
            ps.setBoolean(i++, ri.isHotWater());
            ps.setBoolean(i++, ri.isElectricity());

            ps.setInt(i++, ri.getParkingSpaces());
            ps.setString(i++, ri.getParkingSpaceNumber());
            ps.setBoolean(i++, ri.isRecyclingFacilities());

            ps.setBoolean(i++, ri.isLawnCare());
            ps.setBoolean(i++, ri.isSnowRemoval());
            ps.setBoolean(i++, ri.isGarbageRemoval());
            ps.setString(i++, ri.getOtherCustom());

            ps.setBoolean(i++, tr.isLawnCare());
            ps.setBoolean(i++, tr.isLatePayment());
            ps.setBoolean(i++, tr.isSnowRemoval());
            ps.setBigDecimal(i++, tr.getReturnedChequeLimit());

            ps.setBoolean(i++, tr.isGarbageRemoval());
            ps.setBigDecimal(i++, tr.getParkingFee());
            ps.setInt(i++, tr.getParkingSpaces());
            ps.setBoolean(i++, tr.isTenantInsurance());

            ps.setBigDecimal(i++, tr.getLockoutChargeLimit());
            ps.setBoolean(i++, tr.isSeparationRecyclables());
            ps.setBigDecimal(i++, tr.getSubletExpenseLimit());

            ps.setString(i++, lease.getAdditionalObligations());

            ps.setBoolean(i++, sd.isRequired());
            ps.setBigDecimal(i++, sd.getAmount());
            ps.setString(i++, sd.getInstitution());

            ps.setBoolean(i++, lease.isInspectionReportAttached());
            ps.setBoolean(i++, lease.isRulesAttached());
            ps.setString(i++, lease.getRulesScheduleRef());
            ps.setString(i++, lease.getStatus() != null ? lease.getStatus() : "DRAFT");

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating lease failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1); // newly created lease_id
                } else {
                    throw new SQLException("Creating lease failed, no ID obtained.");
                }
            }
        }
    }

    // Update an existing DRAFT lease with the homeowner's edited values.
    // Only DRAFT leases should ever reach here (enforced by the servlet),
    // so we don't touch homeowner_id or status.
    public boolean updateLease(Lease lease) throws SQLException {
        LandlordInfo l = lease.getLandlordInfo();
        PremisesInfo p = lease.getPremisesInfo();
        LandlordServiceEmail lse = lease.getLandlordServiceEmail();
        LeaseTerm lt = lease.getLeaseTerm();
        RentDetails rd = lease.getRentDetails();
        RentIncludes ri = lease.getRentIncludes();
        TenantResponsibilities tr = lease.getTenantResponsibilities();
        SecurityDeposit sd = lease.getSecurityDeposit();

        String sql = "UPDATE leases SET " +
                "landlord_name = ?, landlord_street_address = ?, landlord_mailing_address = ?, " +
                "landlord_apartment = ?, landlord_city = ?, landlord_postal_code = ?, landlord_phone_home = ?, landlord_phone_business = ?, " +
                "premises_street_address = ?, premises_apartment = ?, premises_city = ?, premises_postal_code = ?, property_type = ?, " +
                "tenant_mailing_pobox = ?, tenant_mailing_postal_code = ?, tenant_phone_home = ?, tenant_phone_business = ?, " +
                "landlord_email_for_service = ?, " +
                "lease_type = ?, periodic_start_date = ?, periodic_term = ?, fixed_start_date = ?, fixed_end_date = ?, " +
                "rent_amount = ?, rent_frequency = ?, payment_method = ?, payment_method_other = ?, rent_due_day = ?, rent_payable_to = ?, " +
                "incl_stove = ?, incl_fridge = ?, incl_washer_dryer = ?, incl_dishwasher = ?, incl_furniture = ?, " +
                "incl_coin_laundry = ?, incl_cable = ?, incl_heat = ?, incl_water = ?, incl_hot_water = ?, incl_electricity = ?, " +
                "incl_parking_spaces = ?, incl_parking_space_number = ?, incl_recycling_facilities = ?, " +
                "incl_lawn_care = ?, incl_snow_removal = ?, incl_garbage_removal = ?, incl_other_custom = ?, " +
                "resp_lawn_care = ?, resp_late_payment = ?, resp_snow_removal = ?, resp_returned_cheque_limit = ?, " +
                "resp_garbage_removal = ?, resp_parking_fee = ?, resp_parking_spaces = ?, resp_tenant_insurance = ?, " +
                "resp_lockout_charge_limit = ?, resp_separation_recyclables = ?, resp_sublet_expense_limit = ?, " +
                "additional_obligations = ?, " +
                "security_deposit_required = ?, security_deposit_amount = ?, security_deposit_institution = ?, " +
                "inspection_report_attached = ?, rules_attached = ?, rules_schedule_ref = ? " +
                "WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, l.getName());
            ps.setString(i++, l.getStreetAddress());
            ps.setString(i++, l.getMailingAddress());
            ps.setString(i++, l.getApartment());
            ps.setString(i++, l.getCity());
            ps.setString(i++, l.getPostalCode());
            ps.setString(i++, l.getPhoneHome());
            ps.setString(i++, l.getPhoneBusiness());

            ps.setString(i++, p.getStreetAddress());
            ps.setString(i++, p.getApartment());
            ps.setString(i++, p.getCity());
            ps.setString(i++, p.getPostalCode());
            ps.setString(i++, p.getPropertyType());

            ps.setString(i++, p.getTenantMailingPobox());
            ps.setString(i++, p.getTenantMailingPostalCode());
            ps.setString(i++, p.getTenantPhoneHome());
            ps.setString(i++, p.getTenantPhoneBusiness());

            ps.setString(i++, lse.getEmailAddress());

            ps.setString(i++, lt.getLeaseType());
            ps.setDate(i++, lt.getPeriodicStartDate());
            ps.setString(i++, lt.getPeriodicTerm());
            ps.setDate(i++, lt.getFixedStartDate());
            ps.setDate(i++, lt.getFixedEndDate());

            ps.setBigDecimal(i++, rd.getAmount());
            ps.setString(i++, rd.getFrequency());
            ps.setString(i++, rd.getPaymentMethod());
            ps.setString(i++, rd.getPaymentMethodOther());
            ps.setInt(i++, rd.getDueDay());
            ps.setString(i++, rd.getPayableTo());

            ps.setBoolean(i++, ri.isStove());
            ps.setBoolean(i++, ri.isFridge());
            ps.setBoolean(i++, ri.isWasherDryer());
            ps.setBoolean(i++, ri.isDishwasher());
            ps.setBoolean(i++, ri.isFurniture());

            ps.setBoolean(i++, ri.isCoinLaundry());
            ps.setBoolean(i++, ri.isCable());
            ps.setBoolean(i++, ri.isHeat());
            ps.setBoolean(i++, ri.isWater());
            ps.setBoolean(i++, ri.isHotWater());
            ps.setBoolean(i++, ri.isElectricity());

            ps.setInt(i++, ri.getParkingSpaces());
            ps.setString(i++, ri.getParkingSpaceNumber());
            ps.setBoolean(i++, ri.isRecyclingFacilities());

            ps.setBoolean(i++, ri.isLawnCare());
            ps.setBoolean(i++, ri.isSnowRemoval());
            ps.setBoolean(i++, ri.isGarbageRemoval());
            ps.setString(i++, ri.getOtherCustom());

            ps.setBoolean(i++, tr.isLawnCare());
            ps.setBoolean(i++, tr.isLatePayment());
            ps.setBoolean(i++, tr.isSnowRemoval());
            ps.setBigDecimal(i++, tr.getReturnedChequeLimit());

            ps.setBoolean(i++, tr.isGarbageRemoval());
            ps.setBigDecimal(i++, tr.getParkingFee());
            ps.setInt(i++, tr.getParkingSpaces());
            ps.setBoolean(i++, tr.isTenantInsurance());

            ps.setBigDecimal(i++, tr.getLockoutChargeLimit());
            ps.setBoolean(i++, tr.isSeparationRecyclables());
            ps.setBigDecimal(i++, tr.getSubletExpenseLimit());

            ps.setString(i++, lease.getAdditionalObligations());

            ps.setBoolean(i++, sd.isRequired());
            ps.setBigDecimal(i++, sd.getAmount());
            ps.setString(i++, sd.getInstitution());

            ps.setBoolean(i++, lease.isInspectionReportAttached());
            ps.setBoolean(i++, lease.isRulesAttached());
            ps.setString(i++, lease.getRulesScheduleRef());

            ps.setInt(i++, lease.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Fetch a lease by its ID
    public Lease getLeaseById(int leaseId) throws SQLException {
        String sql = "SELECT * FROM leases WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    // Fetch all leases created by a given homeowner (for dashboard listing)
    public List<Lease> getLeasesByHomeowner(int homeownerId) throws SQLException {
        String sql = "SELECT * FROM leases WHERE homeowner_id = ? ORDER BY created_at DESC";
        List<Lease> leases = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, homeownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leases.add(mapRow(rs));
                }
            }
        }
        return leases;
    }

    // Update lease status (used when moving through the signing workflow)
    public boolean updateStatus(int leaseId, String status) throws SQLException {
        String sql = "UPDATE leases SET status = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, leaseId);
            return ps.executeUpdate() > 0;
        }
    }

    // Save homeowner's final signature and mark lease completed
    public boolean saveHomeownerSignature(int leaseId, String signatureData, Date signedDate) throws SQLException {
        String sql = "UPDATE leases SET homeowner_signature_data = ?, homeowner_signed_date = ?, status = 'COMPLETED' WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, signatureData);
            ps.setDate(2, signedDate);
            ps.setInt(3, leaseId);
            return ps.executeUpdate() > 0;
        }
    }

    // Maps one row of the leases table back into a composed Lease object
    private Lease mapRow(ResultSet rs) throws SQLException {
        Lease lease = new Lease();
        lease.setId(rs.getInt("id"));
        lease.setHomeownerId(rs.getInt("homeowner_id"));

        LandlordInfo l = lease.getLandlordInfo();
        l.setName(rs.getString("landlord_name"));
        l.setStreetAddress(rs.getString("landlord_street_address"));
        l.setMailingAddress(rs.getString("landlord_mailing_address"));
        l.setApartment(rs.getString("landlord_apartment"));
        l.setCity(rs.getString("landlord_city"));
        l.setPostalCode(rs.getString("landlord_postal_code"));
        l.setPhoneHome(rs.getString("landlord_phone_home"));
        l.setPhoneBusiness(rs.getString("landlord_phone_business"));

        PremisesInfo p = lease.getPremisesInfo();
        p.setStreetAddress(rs.getString("premises_street_address"));
        p.setApartment(rs.getString("premises_apartment"));
        p.setCity(rs.getString("premises_city"));
        p.setPostalCode(rs.getString("premises_postal_code"));
        p.setPropertyType(rs.getString("property_type"));
        p.setTenantMailingPobox(rs.getString("tenant_mailing_pobox"));
        p.setTenantMailingPostalCode(rs.getString("tenant_mailing_postal_code"));
        p.setTenantPhoneHome(rs.getString("tenant_phone_home"));
        p.setTenantPhoneBusiness(rs.getString("tenant_phone_business"));

        lease.getLandlordServiceEmail().setEmailAddress(rs.getString("landlord_email_for_service"));

        com.leaseapp.model.tenant.EmergencyContact ec = lease.getEmergencyContact();
        ec.setNextOfKin(rs.getString("emergency_next_of_kin"));
        ec.setPhoneWork(rs.getString("emergency_phone_work"));
        ec.setPhoneHome(rs.getString("emergency_phone_home"));

        LeaseTerm lt = lease.getLeaseTerm();
        lt.setLeaseType(rs.getString("lease_type"));
        lt.setPeriodicStartDate(rs.getDate("periodic_start_date"));
        lt.setPeriodicTerm(rs.getString("periodic_term"));
        lt.setFixedStartDate(rs.getDate("fixed_start_date"));
        lt.setFixedEndDate(rs.getDate("fixed_end_date"));

        RentDetails rd = lease.getRentDetails();
        rd.setAmount(rs.getBigDecimal("rent_amount"));
        rd.setFrequency(rs.getString("rent_frequency"));
        rd.setPaymentMethod(rs.getString("payment_method"));
        rd.setPaymentMethodOther(rs.getString("payment_method_other"));
        rd.setDueDay(rs.getInt("rent_due_day"));
        rd.setPayableTo(rs.getString("rent_payable_to"));

        RentIncludes ri = lease.getRentIncludes();
        ri.setStove(rs.getBoolean("incl_stove"));
        ri.setFridge(rs.getBoolean("incl_fridge"));
        ri.setWasherDryer(rs.getBoolean("incl_washer_dryer"));
        ri.setDishwasher(rs.getBoolean("incl_dishwasher"));
        ri.setFurniture(rs.getBoolean("incl_furniture"));
        ri.setCoinLaundry(rs.getBoolean("incl_coin_laundry"));
        ri.setCable(rs.getBoolean("incl_cable"));
        ri.setHeat(rs.getBoolean("incl_heat"));
        ri.setWater(rs.getBoolean("incl_water"));
        ri.setHotWater(rs.getBoolean("incl_hot_water"));
        ri.setElectricity(rs.getBoolean("incl_electricity"));
        ri.setParkingSpaces(rs.getInt("incl_parking_spaces"));
        ri.setParkingSpaceNumber(rs.getString("incl_parking_space_number"));
        ri.setRecyclingFacilities(rs.getBoolean("incl_recycling_facilities"));
        ri.setLawnCare(rs.getBoolean("incl_lawn_care"));
        ri.setSnowRemoval(rs.getBoolean("incl_snow_removal"));
        ri.setGarbageRemoval(rs.getBoolean("incl_garbage_removal"));
        ri.setOtherCustom(rs.getString("incl_other_custom"));

        TenantResponsibilities tr = lease.getTenantResponsibilities();
        tr.setLawnCare(rs.getBoolean("resp_lawn_care"));
        tr.setLatePayment(rs.getBoolean("resp_late_payment"));
        tr.setSnowRemoval(rs.getBoolean("resp_snow_removal"));
        tr.setReturnedChequeLimit(rs.getBigDecimal("resp_returned_cheque_limit"));
        tr.setGarbageRemoval(rs.getBoolean("resp_garbage_removal"));
        tr.setParkingFee(rs.getBigDecimal("resp_parking_fee"));
        tr.setParkingSpaces(rs.getInt("resp_parking_spaces"));
        tr.setTenantInsurance(rs.getBoolean("resp_tenant_insurance"));
        tr.setLockoutChargeLimit(rs.getBigDecimal("resp_lockout_charge_limit"));
        tr.setSeparationRecyclables(rs.getBoolean("resp_separation_recyclables"));
        tr.setSubletExpenseLimit(rs.getBigDecimal("resp_sublet_expense_limit"));

        lease.setAdditionalObligations(rs.getString("additional_obligations"));

        SecurityDeposit sd = lease.getSecurityDeposit();
        sd.setRequired(rs.getBoolean("security_deposit_required"));
        sd.setAmount(rs.getBigDecimal("security_deposit_amount"));
        sd.setInstitution(rs.getString("security_deposit_institution"));

        lease.setInspectionReportAttached(rs.getBoolean("inspection_report_attached"));
        lease.setRulesAttached(rs.getBoolean("rules_attached"));
        lease.setRulesScheduleRef(rs.getString("rules_schedule_ref"));

        lease.setStatus(rs.getString("status"));
        lease.setHomeownerSignatureData(rs.getString("homeowner_signature_data"));
        lease.setHomeownerSignedDate(rs.getDate("homeowner_signed_date"));

        return lease;
    }
}