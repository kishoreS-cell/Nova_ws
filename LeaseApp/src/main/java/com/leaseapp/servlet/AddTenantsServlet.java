package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.model.Lease;
import com.leaseapp.model.tenant.Tenant;
import com.leaseapp.util.EmailUtil;
import com.leaseapp.util.TokenUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@WebServlet("/add-tenants")
public class AddTenantsServlet extends HttpServlet {

    private static final int MAX_TENANTS = 3;
    private static final int TOKEN_VALID_DAYS = 14;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getSession().getAttribute("userId") == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        String leaseIdParam = req.getParameter("leaseId");
        if (leaseIdParam == null) {
            resp.sendRedirect("Dashboard.jsp");
            return;
        }

        req.setAttribute("leaseId", leaseIdParam);

        // If tenants were already added for this lease (e.g. homeowner is editing
        // a lease that was already sent out), load them so the form can be
        // pre-filled instead of showing blank fields again.
        try {
            int leaseId = Integer.parseInt(leaseIdParam);
            TenantDAO tenantDAO = new TenantDAO();
            req.setAttribute("existingTenants", tenantDAO.getTenantsByLeaseId(leaseId));
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            // Not fatal - just show the form blank if this lookup fails
        }

        req.getRequestDispatcher("add_tenants.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer homeownerId = (Integer) session.getAttribute("userId");
        if (homeownerId == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        int leaseId;
        try {
            leaseId = Integer.parseInt(req.getParameter("leaseId"));
        } catch (NumberFormatException e) {
            resp.sendRedirect("Dashboard.jsp");
            return;
        }

        LeaseDAO leaseDAO = new LeaseDAO();
        TenantDAO tenantDAO = new TenantDAO();

        try {
            // Ownership check - make sure this lease actually belongs to the logged-in homeowner
            Lease lease = leaseDAO.getLeaseById(leaseId);
            if (lease == null || lease.getHomeownerId() != homeownerId) {
                resp.sendRedirect("Dashboard.jsp");
                return;
            }

            int tenantsAdded = 0;
            java.util.List<String> emailErrors = new java.util.ArrayList<>();

            for (int order = 1; order <= MAX_TENANTS; order++) {
                String name = req.getParameter("tenantName" + order);
                String email = req.getParameter("tenantEmail" + order);

                if (name == null || name.isBlank() || email == null || email.isBlank()) {
                    continue; // this tenant slot was left empty - skip it
                }

                Tenant tenant = new Tenant(leaseId, order, name.trim(), email.trim());
                tenant.setSecureToken(TokenUtil.generateSecureToken());
                tenant.setTokenExpiry(Timestamp.from(Instant.now().plus(TOKEN_VALID_DAYS, ChronoUnit.DAYS)));

                tenantDAO.upsertTenant(tenant);
                tenantsAdded++;

                // Send the tenant their secure link by email.
                try {
                    EmailUtil.sendTenantInviteEmail(tenant.getEmail(), tenant.getFullName(),
                            lease.getPremisesInfo().getStreetAddress(), buildSecureLink(req, tenant.getSecureToken()));
                } catch (MessagingException e) {
                    // Don't fail the whole request just because one email didn't go out -
                    // the tenant record is already saved, so log it and let the homeowner
                    // know rather than silently losing the failure.
                    e.printStackTrace();
                    emailErrors.add(tenant.getEmail());
                }
            }

            if (tenantsAdded == 0) {
                req.setAttribute("error", "Please add at least one tenant.");
                req.setAttribute("leaseId", String.valueOf(leaseId));
                req.getRequestDispatcher("add_tenants.jsp").forward(req, resp);
                return;
            }

            // Lease is no longer a draft - it's waiting on tenant signatures now
            leaseDAO.updateStatus(leaseId, "PENDING_TENANT_SIGNATURES");

            if (!emailErrors.isEmpty()) {
                resp.sendRedirect("Dashboard.jsp?leaseCreated=true&emailFailedFor=" + String.join(",", emailErrors));
            } else {
                resp.sendRedirect("Dashboard.jsp?leaseCreated=true");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Database error: " + e.getMessage());
            req.setAttribute("leaseId", String.valueOf(leaseId));
            req.getRequestDispatcher("add_tenants.jsp").forward(req, resp);
        }
    }

    // Builds the full secure link a tenant will click from their email
    private String buildSecureLink(HttpServletRequest req, String token) {
        String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
        return base + "/tenant-form?token=" + token;
    }
}