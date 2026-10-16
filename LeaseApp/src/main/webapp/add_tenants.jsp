<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.leaseapp.model.tenant.Tenant" %>
<%@ page import="java.util.List" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }
    String leaseId = (String) request.getAttribute("leaseId");
    if (leaseId == null) {
        leaseId = request.getParameter("leaseId");
    }

    // Pre-fill tenant slots if this lease already has tenants saved
    // (homeowner is editing a lease that was already sent out).
    @SuppressWarnings("unchecked")
    List<Tenant> existingTenants = (List<Tenant>) request.getAttribute("existingTenants");
    String[] prefillName = new String[4];
    String[] prefillEmail = new String[4];
    if (existingTenants != null) {
        for (Tenant t : existingTenants) {
            if (t.getTenantOrder() >= 1 && t.getTenantOrder() <= 3) {
                prefillName[t.getTenantOrder()] = t.getFullName();
                prefillEmail[t.getTenantOrder()] = t.getEmail();
            }
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Add Tenants - Lease App</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 30px; }
        .container { max-width: 650px; margin: 0 auto; background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h2 { text-align: center; color: #1e3a8a; }
        p.subtitle { text-align: center; color: #6b7280; margin-top: -10px; }
        fieldset { border: 1px solid #d1d5db; border-radius: 6px; margin-bottom: 18px; padding: 15px 20px; }
        legend { font-weight: bold; color: #1d4ed8; padding: 0 8px; }
        label { display: block; margin-top: 10px; margin-bottom: 4px; font-size: 14px; color: #374151; }
        input[type=text], input[type=email] {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; font-size: 14px;
        }
        button { width: 100%; padding: 12px; background: #2563eb; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; margin-top: 10px; }
        button:hover { background: #1d4ed8; }
        .error { color: red; text-align: center; }
        .hint { font-size: 12px; color: #9ca3af; margin-top: 4px; }
    </style>
</head>
<body>
<div class="container">
    <h2>Add Tenants</h2>
    <p class="subtitle">Add up to 3 tenants. Each will receive a secure link by email to complete and sign their section.</p>

    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>

    <form action="add-tenants" method="post">
        <input type="hidden" name="leaseId" value="<%= leaseId %>">

        <fieldset>
            <legend>Tenant 1</legend>
            <label>Full Name</label>
            <input type="text" name="tenantName1" value="<%= prefillName[1] != null ? prefillName[1] : "" %>" required>
            <label>Email Address</label>
            <input type="email" name="tenantEmail1" value="<%= prefillEmail[1] != null ? prefillEmail[1] : "" %>" required>
        </fieldset>

        <fieldset>
            <legend>Tenant 2 <span class="hint">(optional)</span></legend>
            <label>Full Name</label>
            <input type="text" name="tenantName2" value="<%= prefillName[2] != null ? prefillName[2] : "" %>">
            <label>Email Address</label>
            <input type="email" name="tenantEmail2" value="<%= prefillEmail[2] != null ? prefillEmail[2] : "" %>">
        </fieldset>

        <fieldset>
            <legend>Tenant 3 <span class="hint">(optional)</span></legend>
            <label>Full Name</label>
            <input type="text" name="tenantName3" value="<%= prefillName[3] != null ? prefillName[3] : "" %>">
            <label>Email Address</label>
            <input type="email" name="tenantEmail3" value="<%= prefillEmail[3] != null ? prefillEmail[3] : "" %>">
        </fieldset>

        <button type="submit">Save and Send Invites</button>
    </form>
</div>
</body>
</html>
