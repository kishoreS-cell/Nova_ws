package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.OccupantDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.model.Lease;
import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.model.tenant.Tenant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Shows the homeowner a read-only, all-sections-filled-in view of a lease -
// combining what the homeowner entered when creating it with whatever each
// tenant has since filled in and signed. From here the homeowner can add
// their own final signature (once every tenant has signed) and download the PDF.
@WebServlet("/view-lease")
public class ViewLeaseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
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

            List<Tenant> tenants = tenantDAO.getTenantsByLeaseId(leaseId);
            List<Occupant> occupants = occupantDAO.getOccupantsByLeaseId(leaseId);

            req.setAttribute("lease", lease);
            req.setAttribute("tenants", tenants);
            req.setAttribute("occupants", occupants);
            req.setAttribute("allTenantsSigned", tenantDAO.allTenantsSigned(leaseId));
            req.getRequestDispatcher("/view_lease.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}