package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.OccupantDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.model.Lease;
import com.leaseapp.model.User;
import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.model.tenant.Tenant;
import com.leaseapp.util.EmailUtil;
import com.leaseapp.util.LeasePdfGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Called when the homeowner draws their signature on view_lease.jsp, after every
// tenant has already signed. Saves the signature, marks the lease COMPLETED, then
// emails the final signed PDF to the homeowner and every tenant.
@WebServlet("/homeowner-sign")
public class HomeownerSignServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getSession().getAttribute("userId") == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        int leaseId;
        try {
            leaseId = Integer.parseInt(req.getParameter("leaseId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lease ID");
            return;
        }

        String signatureData = req.getParameter("signatureData");
        if (signatureData == null || signatureData.isBlank()) {
            resp.sendRedirect("view-lease?leaseId=" + leaseId + "&signError=true");
            return;
        }

        try {
            LeaseDAO leaseDAO = new LeaseDAO();
            TenantDAO tenantDAO = new TenantDAO();
            OccupantDAO occupantDAO = new OccupantDAO();

            Lease lease = leaseDAO.getLeaseById(leaseId);
            if (lease == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Lease not found");
                return;
            }

            Integer homeownerId = (Integer) req.getSession().getAttribute("userId");
            if (lease.getHomeownerId() != homeownerId) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (!"PENDING_HOMEOWNER_SIGNATURE".equals(lease.getStatus())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This lease is not ready for the homeowner's signature yet.");
                return;
            }
            if (!tenantDAO.allTenantsSigned(leaseId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Every tenant must sign before the homeowner can sign.");
                return;
            }

            Date signedDate = new Date(System.currentTimeMillis());
            leaseDAO.saveHomeownerSignature(leaseId, signatureData, signedDate);

            // Re-fetch so the generated PDF reflects the signature and COMPLETED status we just saved
            lease = leaseDAO.getLeaseById(leaseId);
            List<Tenant> tenants = tenantDAO.getTenantsByLeaseId(leaseId);
            List<Occupant> occupants = occupantDAO.getOccupantsByLeaseId(leaseId);

            try {
                byte[] pdfBytes = new LeasePdfGenerator().generate(lease, tenants, occupants);

                User homeowner = (User) req.getSession().getAttribute("user");
                List<String> recipients = new ArrayList<>();
                if (homeowner != null && homeowner.getEmail() != null) {
                    recipients.add(homeowner.getEmail());
                }
                for (Tenant t : tenants) {
                    if (t.getEmail() != null && !t.getEmail().isBlank()) {
                        recipients.add(t.getEmail());
                    }
                }

                String address = lease.getPremisesInfo().getStreetAddress() != null
                        ? lease.getPremisesInfo().getStreetAddress() : "your rental property";

                if (!recipients.isEmpty()) {
                    EmailUtil.sendCompletedLeaseEmail(recipients, address, pdfBytes);
                }
            } catch (Exception emailEx) {
                // The lease is already saved as COMPLETED - a failed email shouldn't block that.
                emailEx.printStackTrace();
            }

            resp.sendRedirect("view-lease?leaseId=" + leaseId + "&completed=true");

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}