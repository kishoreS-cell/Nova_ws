package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.OccupantDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.dao.UserDAO;
import com.leaseapp.model.Lease;
import com.leaseapp.model.User;
import com.leaseapp.model.tenant.AttachmentAcknowledgment;
import com.leaseapp.model.tenant.EmergencyContact;
import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.model.tenant.Tenant;
import com.leaseapp.util.EmailUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

@WebServlet("/tenant-form")
public class TenantFormServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing token");
            return;
        }

        try {
            TenantDAO tenantDAO = new TenantDAO();
            Tenant tenant = tenantDAO.getTenantByToken(token);

            if (tenant == null) {
                req.setAttribute("linkError", "This link is invalid.");
                req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
                return;
            }
            if (tenant.getTokenExpiry() != null && tenant.getTokenExpiry().before(new Timestamp(System.currentTimeMillis()))) {
                req.setAttribute("linkError", "This link has expired. Please contact the homeowner for a new one.");
                req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
                return;
            }
            if (tenant.isSigned()) {
                if (req.getParameter("submitted") != null) {
                    req.setAttribute("submittedSuccess", true);
                    req.setAttribute("tenant", tenant);
                    req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
                    return;
                }
                req.setAttribute("linkError", "You have already completed and signed this application.");
                req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
                return;
            }

            LeaseDAO leaseDAO = new LeaseDAO();
            Lease lease = leaseDAO.getLeaseById(tenant.getLeaseId());

            req.setAttribute("tenant", tenant);
            req.setAttribute("lease", lease);
            req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("linkError", "Something went wrong. Please try again later.");
            req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing token");
            return;
        }

        TenantDAO tenantDAO = new TenantDAO();
        LeaseDAO leaseDAO = new LeaseDAO();
        OccupantDAO occupantDAO = new OccupantDAO();

        try {
            Tenant tenant = tenantDAO.getTenantByToken(token);
            if (tenant == null || tenant.isSigned()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This link is no longer valid.");
                return;
            }

            // ---------- Section 8 - Email for service ----------
            tenant.setEmailForService(req.getParameter("emailForService"));

            // ---------- Section 26 - Attachment acknowledgment ----------
            AttachmentAcknowledgment ack = tenant.getAttachmentAcknowledgment();
            ack.setReceivedAct(req.getParameter("ackReceivedAct") != null);
            ack.setReceivedSignedCopy(req.getParameter("ackReceivedSignedCopy") != null);
            ack.setReceivedRules(req.getParameter("ackReceivedRules") != null);

            // ---------- Signature ----------
            String signatureData = req.getParameter("signatureData");
            if (signatureData == null || signatureData.isBlank()) {
                req.setAttribute("error", "Please provide your signature before submitting.");
                req.setAttribute("tenant", tenant);
                req.setAttribute("lease", leaseDAO.getLeaseById(tenant.getLeaseId()));
                req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
                return;
            }
            tenant.setSignatureData(signatureData);
            tenant.setSigned(true);
            tenant.setSignedDate(new Date(System.currentTimeMillis()));

            tenantDAO.updateTenantSubmission(tenant);

            // ---------- Section 4 - Emergency contact (stored at lease level) ----------
            EmergencyContact ec = new EmergencyContact();
            ec.setNextOfKin(req.getParameter("emergencyNextOfKin"));
            ec.setPhoneWork(req.getParameter("emergencyPhoneWork"));
            ec.setPhoneHome(req.getParameter("emergencyPhoneHome"));
            leaseDAO.updateEmergencyContact(tenant.getLeaseId(), ec);

            // ---------- Section 2 - Occupants (age rule: under 18 only) ----------
            String[] names = req.getParameterValues("occupantName");
            String[] ages = req.getParameterValues("occupantAge");
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    if (names[i] == null || names[i].isBlank()) continue;
                    int age = 0;
                    try { age = Integer.parseInt(ages[i]); } catch (Exception ignored) {}

                    if (age >= 18) {
                        // Per the age rule, 18+ must be added as a Tenant by the homeowner,
                        // not listed here as an occupant - skip silently rather than fail the whole submission.
                        continue;
                    }
                    Occupant occupant = new Occupant(names[i].trim(), age);
                    occupant.setLeaseId(tenant.getLeaseId());
                    occupantDAO.addOccupant(occupant);
                }
            }

            // ---------- Check if this was the last tenant to sign ----------
            if (tenantDAO.allTenantsSigned(tenant.getLeaseId())) {
                leaseDAO.updateStatus(tenant.getLeaseId(), "PENDING_HOMEOWNER_SIGNATURE");

                try {
                    Lease lease = leaseDAO.getLeaseById(tenant.getLeaseId());
                    UserDAO userDAO = new UserDAO();
                    User homeowner = userDAO.getUserById(lease.getHomeownerId());

                    if (homeowner != null && homeowner.getEmail() != null) {
                        String address = lease.getPremisesInfo().getStreetAddress() != null
                                ? lease.getPremisesInfo().getStreetAddress() : "your rental property";

                        EmailUtil.sendHomeownerReviewNotification(
                                homeowner.getEmail(),
                                homeowner.getFullName(),
                                address,
                                buildReviewLink(req, tenant.getLeaseId()));
                    }
                } catch (MessagingException e) {
                    // The lease status is already saved as PENDING_HOMEOWNER_SIGNATURE -
                    // a failed email shouldn't block the tenant's submission.
                    e.printStackTrace();
                }
            }

            resp.sendRedirect("tenant-form?token=" + token + "&submitted=true");

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Database error: " + e.getMessage());
            req.getRequestDispatcher("/tenant_form.jsp").forward(req, resp);
        }
    }

    private String buildReviewLink(HttpServletRequest req, int leaseId) {
        String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
        return base + "/view-lease?leaseId=" + leaseId;
    }
}