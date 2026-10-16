package com.leaseapp.dao;

import com.leaseapp.model.tenant.AttachmentAcknowledgment;
import com.leaseapp.model.tenant.Tenant;
import com.leaseapp.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    // Insert a new tenant row for a lease (called once per tenant when homeowner adds them)
    public int addTenant(Tenant tenant) throws SQLException {
        String sql = "INSERT INTO tenants (lease_id, tenant_order, full_name, email, secure_token, token_expiry) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tenant.getLeaseId());
            ps.setInt(2, tenant.getTenantOrder());
            ps.setString(3, tenant.getFullName());
            ps.setString(4, tenant.getEmail());
            ps.setString(5, tenant.getSecureToken());
            ps.setTimestamp(6, tenant.getTokenExpiry());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating tenant failed, no rows affected.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Creating tenant failed, no ID obtained.");
                }
            }
        }
    }

    // Insert-or-update a tenant slot for a lease. First time a homeowner adds
    // tenant N, this inserts; if the homeowner later edits the (still-unsigned)
    // lease and re-submits Add Tenants, this updates the existing row instead
    // of failing on the (lease_id, tenant_order) unique constraint. A fresh
    // secure token/expiry is written each time, which is fine here since this
    // path only runs before any tenant has signed.
    public int upsertTenant(Tenant tenant) throws SQLException {
        String sql = "INSERT INTO tenants (lease_id, tenant_order, full_name, email, secure_token, token_expiry) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "full_name = VALUES(full_name), email = VALUES(email), " +
                     "secure_token = VALUES(secure_token), token_expiry = VALUES(token_expiry), " +
                     "id = LAST_INSERT_ID(id)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tenant.getLeaseId());
            ps.setInt(2, tenant.getTenantOrder());
            ps.setString(3, tenant.getFullName());
            ps.setString(4, tenant.getEmail());
            ps.setString(5, tenant.getSecureToken());
            ps.setTimestamp(6, tenant.getTokenExpiry());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1); // works for both the insert and the update case, via LAST_INSERT_ID(id)
                } else {
                    throw new SQLException("Saving tenant failed, no ID obtained.");
                }
            }
        }
    }

    // Fetch all tenants for a given lease (ordered by tenant_order: 1, 2, 3)
    public List<Tenant> getTenantsByLeaseId(int leaseId) throws SQLException {
        String sql = "SELECT * FROM tenants WHERE lease_id = ? ORDER BY tenant_order ASC";
        List<Tenant> tenants = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tenants.add(mapRow(rs));
                }
            }
        }
        return tenants;
    }

    // Fetch a single tenant by their secure token (used when tenant opens their email link)
    public Tenant getTenantByToken(String token) throws SQLException {
        String sql = "SELECT * FROM tenants WHERE secure_token = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    // Checks if every tenant for a lease has signed (used to trigger homeowner notification)
    public boolean allTenantsSigned(int leaseId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, SUM(CASE WHEN is_signed THEN 1 ELSE 0 END) AS signed " +
                     "FROM tenants WHERE lease_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int signed = rs.getInt("signed");
                    return total > 0 && total == signed;
                }
                return false;
            }
        }
    }

    // Checks if at least one tenant has already signed. Once this is true, the
    // homeowner should no longer be able to edit the lease terms - a tenant has
    // already committed to what they saw, so changing it underneath them isn't safe.
    // Before any tenant has signed, editing is still fine even if the lease has
    // already been sent out (status PENDING_TENANT_SIGNATURES).
    public boolean anyTenantSigned(int leaseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tenants WHERE lease_id = ? AND is_signed = TRUE";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    // Updates a tenant's Section 1(remaining)/8/26 fields plus signature - called when
    // the tenant submits their form via the secure link
    public boolean updateTenantSubmission(Tenant tenant) throws SQLException {
        AttachmentAcknowledgment ack = tenant.getAttachmentAcknowledgment();

        String sql = "UPDATE tenants SET " +
                "email_for_service = ?, " +
                "ack_received_act = ?, ack_received_signed_copy = ?, ack_received_rules = ?, " +
                "is_signed = ?, signature_data = ?, signed_date = ? " +
                "WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenant.getEmailForService());
            ps.setBoolean(2, ack.isReceivedAct());
            ps.setBoolean(3, ack.isReceivedSignedCopy());
            ps.setBoolean(4, ack.isReceivedRules());
            ps.setBoolean(5, tenant.isSigned());
            ps.setString(6, tenant.getSignatureData());
            ps.setDate(7, tenant.getSignedDate());
            ps.setInt(8, tenant.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Maps one row of the tenants table to a Tenant object
    private Tenant mapRow(ResultSet rs) throws SQLException {
        Tenant tenant = new Tenant();
        tenant.setId(rs.getInt("id"));
        tenant.setLeaseId(rs.getInt("lease_id"));
        tenant.setTenantOrder(rs.getInt("tenant_order"));
        tenant.setFullName(rs.getString("full_name"));
        tenant.setEmail(rs.getString("email"));
        tenant.setEmailForService(rs.getString("email_for_service"));

        AttachmentAcknowledgment ack = tenant.getAttachmentAcknowledgment();
        ack.setReceivedAct(rs.getBoolean("ack_received_act"));
        ack.setReceivedSignedCopy(rs.getBoolean("ack_received_signed_copy"));
        ack.setReceivedRules(rs.getBoolean("ack_received_rules"));

        tenant.setSecureToken(rs.getString("secure_token"));
        tenant.setTokenExpiry(rs.getTimestamp("token_expiry"));

        tenant.setSigned(rs.getBoolean("is_signed"));
        tenant.setSignatureData(rs.getString("signature_data"));
        tenant.setSignedDate(rs.getDate("signed_date"));

        return tenant;
    }
}