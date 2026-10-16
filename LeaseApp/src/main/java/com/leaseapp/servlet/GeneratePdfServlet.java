package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.OccupantDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.model.Lease;
import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.model.tenant.Tenant;
import com.leaseapp.util.LeasePdfGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/generate-pdf")
public class GeneratePdfServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getSession().getAttribute("userId") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int leaseId;
        try {
            leaseId = Integer.parseInt(req.getParameter("leaseId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lease ID");
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

            // Ownership check
            Integer homeownerId = (Integer) req.getSession().getAttribute("userId");
            if (lease.getHomeownerId() != homeownerId) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            List<Tenant> tenants = tenantDAO.getTenantsByLeaseId(leaseId);
            List<Occupant> occupants = occupantDAO.getOccupantsByLeaseId(leaseId);

            LeasePdfGenerator generator = new LeasePdfGenerator();
            byte[] pdfBytes = generator.generate(lease, tenants, occupants);

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"lease-agreement-" + leaseId + ".pdf\"");
            resp.setContentLength(pdfBytes.length);

            try (OutputStream out = resp.getOutputStream()) {
                out.write(pdfBytes);
                out.flush();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF generation error: " + e.getMessage());
        }
    }
}