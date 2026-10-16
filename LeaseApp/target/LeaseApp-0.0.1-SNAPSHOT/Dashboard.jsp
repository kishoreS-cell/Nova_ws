<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.leaseapp.dao.LeaseDAO" %>
<%@ page import="com.leaseapp.dao.TenantDAO" %>
<%@ page import="com.leaseapp.model.Lease" %>
<%@ page import="java.util.List" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }
    Integer homeownerId = (Integer) session.getAttribute("userId");

    List<Lease> leases;
    try {
        leases = new LeaseDAO().getLeasesByHomeowner(homeownerId);
    } catch (Exception e) {
        e.printStackTrace();
        leases = java.util.Collections.emptyList();
    }
    TenantDAO tenantDAO = new TenantDAO();
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - Lease App</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 40px; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 700px; margin: 0 auto; }
        a { color: #2563eb; text-decoration: none; }
        .top-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { text-align: left; padding: 10px 8px; border-bottom: 1px solid #e5e7eb; font-size: 14px; }
        th { color: #6b7280; font-weight: 600; }
        .status { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-DRAFT { background: #fef3c7; color: #92400e; }
        .status-PENDING_TENANT_SIGNATURES { background: #dbeafe; color: #1e40af; }
        .status-PENDING_HOMEOWNER_SIGNATURE { background: #ede9fe; color: #5b21b6; }
        .status-COMPLETED { background: #d1fae5; color: #065f46; }
        .empty { color: #6b7280; text-align: center; padding: 30px 0; }
        button, .btn { padding: 8px 14px; background: #2563eb; color: white; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
        .btn-secondary { background: #6b7280; }
        .success { color: #065f46; background: #d1fae5; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="top-actions">
            <h2>Welcome, <%= session.getAttribute("userName") %>!</h2>
            <a href="Logout">Logout</a>
        </div>

        <% if (request.getParameter("leaseCreated") != null) { %>
            <p class="success">Lease created. Add tenants to send it out for signing.</p>
        <% } %>
        <% if (request.getParameter("leaseUpdated") != null) { %>
            <p class="success">Lease updated.</p>
        <% } %>

        <a href="create-lease"><button>Create New Lease</button></a>

        <h3 style="margin-top: 30px;">Your Leases</h3>

        <% if (leases.isEmpty()) { %>
            <p class="empty">You haven't created any leases yet.</p>
        <% } else { %>
            <table>
                <tr>
                    <th>Property</th>
                    <th>Rent</th>
                    <th>Status</th>
                    <th></th>
                </tr>
                <% for (Lease lease : leases) { %>
                    <%
                        // A lease is still editable as long as no tenant has signed yet -
                        // DRAFT is always editable, and PENDING_TENANT_SIGNATURES is too
                        // until the first signature comes in.
                        boolean editable;
                        try {
                            editable = "DRAFT".equals(lease.getStatus())
                                    || ("PENDING_TENANT_SIGNATURES".equals(lease.getStatus())
                                        && !tenantDAO.anyTenantSigned(lease.getId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            editable = "DRAFT".equals(lease.getStatus());
                        }
                    %>
                    <tr>
                        <td>
                            <%= lease.getPremisesInfo().getStreetAddress() != null ? lease.getPremisesInfo().getStreetAddress() : "(no address)" %><br>
                            <small style="color:#6b7280;"><%= lease.getPremisesInfo().getCity() != null ? lease.getPremisesInfo().getCity() : "" %></small>
                        </td>
                        <td>
                            <%= lease.getRentDetails().getAmount() != null ? "$" + lease.getRentDetails().getAmount() : "-" %>
                        </td>
                        <td>
                            <span class="status status-<%= lease.getStatus() %>"><%= lease.getStatus() %></span>
                        </td>
                        <td>
                            <% if (editable) { %>
                                <a href="create-lease?leaseId=<%= lease.getId() %>"><button class="btn-secondary">Edit</button></a>
                            <% } else { %>
                                <a href="view-lease?leaseId=<%= lease.getId() %>"><button class="btn-secondary">View</button></a>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            </table>
        <% } %>
    </div>
</body>
</html>
