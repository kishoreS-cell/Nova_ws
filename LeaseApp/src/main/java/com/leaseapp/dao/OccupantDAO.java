package com.leaseapp.dao;

import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OccupantDAO {

    public int addOccupant(Occupant occupant) throws SQLException {
        String sql = "INSERT INTO occupants (lease_id, full_name, age) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, occupant.getLeaseId());
            ps.setString(2, occupant.getFullName());
            ps.setInt(3, occupant.getAge());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Creating occupant failed, no ID obtained.");
            }
        }
    }

    public List<Occupant> getOccupantsByLeaseId(int leaseId) throws SQLException {
        String sql = "SELECT * FROM occupants WHERE lease_id = ?";
        List<Occupant> occupants = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Occupant o = new Occupant();
                    o.setId(rs.getInt("id"));
                    o.setLeaseId(rs.getInt("lease_id"));
                    o.setFullName(rs.getString("full_name"));
                    o.setAge(rs.getInt("age"));
                    occupants.add(o);
                }
            }
        }
        return occupants;
    }
}