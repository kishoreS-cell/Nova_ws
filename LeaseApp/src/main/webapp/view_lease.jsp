<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.leaseapp.model.Lease" %>
<%@ page import="com.leaseapp.model.tenant.Tenant" %>
<%@ page import="com.leaseapp.model.tenant.Occupant" %>
<%@ page import="com.leaseapp.model.homeowner.*" %>
<%@ page import="java.util.List" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }
    Lease lease = (Lease) request.getAttribute("lease");
    List<Tenant> tenants = (List<Tenant>) request.getAttribute("tenants");
    List<Occupant> occupants = (List<Occupant>) request.getAttribute("occupants");
    Boolean allTenantsSigned = (Boolean) request.getAttribute("allTenantsSigned");
    if (allTenantsSigned == null) allTenantsSigned = false;

    LandlordInfo landlord = lease.getLandlordInfo();
    PremisesInfo premises = lease.getPremisesInfo();
    LeaseTerm term = lease.getLeaseTerm();
    RentDetails rent = lease.getRentDetails();
    RentIncludes includes = lease.getRentIncludes();
    TenantResponsibilities resp = lease.getTenantResponsibilities();
    SecurityDeposit deposit = lease.getSecurityDeposit();

    boolean canHomeownerSign = "PENDING_HOMEOWNER_SIGNATURE".equals(lease.getStatus()) && allTenantsSigned;
    boolean completed = "COMPLETED".equals(lease.getStatus());
%>
<!DOCTYPE html>
<html>
<head>
    <title>View Lease - Lease App</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 30px; }
        .container { max-width: 800px; margin: 0 auto; }
        .top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        a { color: #2563eb; text-decoration: none; }
        .card { background: white; padding: 24px 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.08); margin-bottom: 18px; }
        h2 { margin-top: 0; color: #1e3a8a; }
        h3 { color: #1d4ed8; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; margin-top: 0; }
        .status { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .status-DRAFT { background: #fef3c7; color: #92400e; }
        .status-PENDING_TENANT_SIGNATURES { background: #dbeafe; color: #1e40af; }
        .status-PENDING_HOMEOWNER_SIGNATURE { background: #ede9fe; color: #5b21b6; }
        .status-COMPLETED { background: #d1fae5; color: #065f46; }
        table.kv { width: 100%; border-collapse: collapse; }
        table.kv td { padding: 6px 4px; font-size: 14px; vertical-align: top; }
        table.kv td.label { color: #6b7280; width: 45%; }
        table.kv td.value { font-weight: 600; color: #111827; }
        .tenant-block { border: 1px solid #e5e7eb; border-radius: 6px; padding: 14px 16px; margin-bottom: 12px; }
        .tenant-block h4 { margin: 0 0 8px 0; }
        .pill { font-size: 11px; padding: 2px 8px; border-radius: 10px; margin-left: 8px; }
        .pill-signed { background: #d1fae5; color: #065f46; }
        .pill-pending { background: #fef3c7; color: #92400e; }
        img.sig { max-height: 60px; border: 1px solid #e5e7eb; border-radius: 4px; margin-top: 6px; background: #fff; }
        button, .btn { padding: 10px 16px; background: #2563eb; color: white; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
        button:hover { background: #1d4ed8; }
        .btn-secondary { background: #6b7280; }
        .success { color: #065f46; background: #d1fae5; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .error { color: #991b1b; background: #fee2e2; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .info-box { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 12px 16px; font-size: 14px; color: #1e40af; }
        canvas { border: 1px dashed #9ca3af; border-radius: 4px; touch-action: none; cursor: crosshair; background: #fff; }
        .sig-actions { margin-top: 8px; display: flex; gap: 10px; }
    </style>
</head>
<body>
<div class="container">

    <div class="top-bar">
        <h2 style="margin:0;">Lease Review</h2>
        <a href="Dashboard.jsp">&larr; Back to Dashboard</a>
    </div>

    <% if (request.getParameter("completed") != null) { %>
        <p class="success">You've signed! The completed lease has been emailed to you and every tenant.</p>
    <% } %>
    <% if (request.getParameter("signError") != null) { %>
        <p class="error">Please provide your signature before submitting.</p>
    <% } %>

    <div class="card">
        <div style="display:flex; justify-content:space-between; align-items:center;">
            <div>
                <h2 style="margin:0;"><%= premises.getStreetAddress() != null ? premises.getStreetAddress() : "(no address)" %></h2>
                <div style="color:#6b7280;"><%= premises.getCity() != null ? premises.getCity() : "" %></div>
            </div>
            <span class="status status-<%= lease.getStatus() %>"><%= lease.getStatus() %></span>
        </div>
        <div style="margin-top:14px;">
            <a href="generate-pdf?leaseId=<%= lease.getId() %>"><button><%= completed ? "Download Signed PDF" : "Download PDF (draft)" %></button></a>
        </div>
    </div>

    <% if (completed) { %>
        <div class="card">
            <h3>Signed Lease Document</h3>
            <embed src="generate-pdf?leaseId=<%= lease.getId() %>" type="application/pdf"
                   style="width:100%; height:1000px; border:1px solid #e5e7eb; border-radius:6px;">
            <p style="font-size:13px; color:#6b7280; margin-top:8px;">
                If the document doesn't display above, <a href="generate-pdf?leaseId=<%= lease.getId() %>">open it directly</a>.
            </p>
        </div>
    <% } else { %>

    <div class="card">
        <h3>Landlord (Section 1, 3)</h3>
        <table class="kv">
            <tr><td class="label">Name</td><td class="value"><%= landlord.getName() %></td></tr>
            <tr><td class="label">Street Address</td><td class="value"><%= landlord.getStreetAddress() %></td></tr>
            <tr><td class="label">Mailing Address</td><td class="value"><%= landlord.getMailingAddress() %></td></tr>
            <tr><td class="label">City</td><td class="value"><%= landlord.getCity() %></td></tr>
            <tr><td class="label">Postal Code</td><td class="value"><%= landlord.getPostalCode() %></td></tr>
            <tr><td class="label">Phone (home)</td><td class="value"><%= landlord.getPhoneHome() %></td></tr>
            <tr><td class="label">Phone (business)</td><td class="value"><%= landlord.getPhoneBusiness() %></td></tr>
        </table>
        <h3 style="margin-top:18px;">Premises</h3>
        <table class="kv">
            <tr><td class="label">Property Type</td><td class="value"><%= premises.getPropertyType() %></td></tr>
            <tr><td class="label">Apartment</td><td class="value"><%= premises.getApartment() %></td></tr>
            <tr><td class="label">Postal Code</td><td class="value"><%= premises.getPostalCode() %></td></tr>
        </table>
    </div>

    <div class="card">
        <h3>Tenants &amp; Occupants (Section 1, 2)</h3>
        <% for (Tenant t : tenants) { %>
            <div class="tenant-block">
                <h4><%= t.getFullName() %>
                    <span class="pill <%= t.isSigned() ? "pill-signed" : "pill-pending" %>">
                        <%= t.isSigned() ? "Signed" : "Awaiting signature" %>
                    </span>
                </h4>
                <table class="kv">
                    <tr><td class="label">Email</td><td class="value"><%= t.getEmail() %></td></tr>
                    <tr><td class="label">Email for service (Sec. 8)</td><td class="value"><%= t.getEmailForService() != null ? t.getEmailForService() : "-" %></td></tr>
                    <tr><td class="label">Received Act / signed copy / rules (Sec. 26)</td>
                        <td class="value">
                            <%= t.getAttachmentAcknowledgment().isReceivedAct() ? "Yes" : "No" %> /
                            <%= t.getAttachmentAcknowledgment().isReceivedSignedCopy() ? "Yes" : "No" %> /
                            <%= t.getAttachmentAcknowledgment().isReceivedRules() ? "Yes" : "No" %>
                        </td>
                    </tr>
                </table>
                <% if (t.isSigned() && t.getSignatureData() != null) { %>
                    <div>
                        <small style="color:#6b7280;">Signed <%= t.getSignedDate() %></small><br>
                        <img class="sig" src="<%= t.getSignatureData() %>" alt="Tenant signature">
                    </div>
                <% } %>
            </div>
        <% } %>

        <% if (occupants != null && !occupants.isEmpty()) { %>
            <h4>Other Occupants (under 18)</h4>
            <ul>
                <% for (Occupant o : occupants) { %>
                    <li><%= o.getFullName() %> (age <%= o.getAge() %>)</li>
                <% } %>
            </ul>
        <% } %>
    </div>

    <div class="card">
        <h3>Emergency Contact (Section 4)</h3>
        <table class="kv">
            <tr><td class="label">Next of Kin</td><td class="value"><%= lease.getEmergencyContact().getNextOfKin() != null ? lease.getEmergencyContact().getNextOfKin() : "-" %></td></tr>
            <tr><td class="label">Phone (work)</td><td class="value"><%= lease.getEmergencyContact().getPhoneWork() != null ? lease.getEmergencyContact().getPhoneWork() : "-" %></td></tr>
            <tr><td class="label">Phone (home)</td><td class="value"><%= lease.getEmergencyContact().getPhoneHome() != null ? lease.getEmergencyContact().getPhoneHome() : "-" %></td></tr>
        </table>
    </div>

    <div class="card">
        <h3>Lease Term (Section 11A/11B) &amp; Rent (Section 13)</h3>
        <table class="kv">
            <tr><td class="label">Lease Type</td><td class="value"><%= term.getLeaseType() %></td></tr>
            <% if ("PERIODIC".equals(term.getLeaseType())) { %>
                <tr><td class="label">Start Date</td><td class="value"><%= term.getPeriodicStartDate() %></td></tr>
                <tr><td class="label">Term</td><td class="value"><%= term.getPeriodicTerm() %></td></tr>
            <% } else { %>
                <tr><td class="label">Start Date</td><td class="value"><%= term.getFixedStartDate() %></td></tr>
                <tr><td class="label">End Date</td><td class="value"><%= term.getFixedEndDate() %></td></tr>
            <% } %>
            <tr><td class="label">Rent Amount</td><td class="value"><%= rent.getAmount() != null ? "$" + rent.getAmount() : "-" %> (<%= rent.getFrequency() %>)</td></tr>
            <tr><td class="label">Payment Method</td><td class="value"><%= rent.getPaymentMethod() %></td></tr>
            <tr><td class="label">Due Day</td><td class="value"><%= rent.getDueDay() %></td></tr>
            <tr><td class="label">Payable To</td><td class="value"><%= rent.getPayableTo() %></td></tr>
        </table>
    </div>

    <div class="card">
        <h3>Security Deposit (18) &amp; Inspection (19)</h3>
        <table class="kv">
            <tr><td class="label">Deposit Required</td><td class="value"><%= deposit.isRequired() ? "Yes - $" + deposit.getAmount() : "No" %></td></tr>
            <tr><td class="label">Inspection Report Attached</td><td class="value"><%= lease.isInspectionReportAttached() ? "Yes" : "No" %></td></tr>
        </table>
        <% if (lease.getAdditionalObligations() != null && !lease.getAdditionalObligations().isBlank()) { %>
            <h4>Additional Obligations (17)</h4>
            <p><%= lease.getAdditionalObligations() %></p>
        <% } %>
    </div>

    <div class="card">
        <h3>Signatures</h3>

        <% if (canHomeownerSign) { %>
            <div class="info-box" style="margin-bottom:14px;">All tenants have signed. Add your signature to complete the lease.</div>
            <form id="homeownerSignForm" action="homeowner-sign" method="post">
                <input type="hidden" name="leaseId" value="<%= lease.getId() %>">
                <input type="hidden" name="signatureData" id="signatureData">
                <canvas id="sigCanvas" width="600" height="150"></canvas>
                <div class="sig-actions">
                    <button type="button" class="btn-secondary" onclick="clearSignature()">Clear</button>
                </div>
                <div style="margin-top:14px;">
                    <button type="submit">Sign &amp; Complete Lease</button>
                </div>
            </form>

        <% } else { %>
            <div class="info-box">Waiting for all tenants to sign before you can add your final signature.</div>
        <% } %>
    </div>

    <% } %>

</div>

<script>
    var canvas = document.getElementById('sigCanvas');
    if (canvas) {
        var ctx = canvas.getContext('2d');
        var drawing = false;

        function getPos(e) {
            var rect = canvas.getBoundingClientRect();
            var x = (e.touches ? e.touches[0].clientX : e.clientX) - rect.left;
            var y = (e.touches ? e.touches[0].clientY : e.clientY) - rect.top;
            return { x: x, y: y };
        }
        function startDraw(e) {
            drawing = true;
            var pos = getPos(e);
            ctx.beginPath();
            ctx.moveTo(pos.x, pos.y);
            e.preventDefault();
        }
        function draw(e) {
            if (!drawing) return;
            var pos = getPos(e);
            ctx.lineTo(pos.x, pos.y);
            ctx.strokeStyle = '#1e3a8a';
            ctx.lineWidth = 2;
            ctx.stroke();
            e.preventDefault();
        }
        function stopDraw() { drawing = false; }

        canvas.addEventListener('mousedown', startDraw);
        canvas.addEventListener('mousemove', draw);
        canvas.addEventListener('mouseup', stopDraw);
        canvas.addEventListener('mouseleave', stopDraw);
        canvas.addEventListener('touchstart', startDraw);
        canvas.addEventListener('touchmove', draw);
        canvas.addEventListener('touchend', stopDraw);

        function clearSignature() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
        }

        document.getElementById('homeownerSignForm').addEventListener('submit', function(e) {
            var blank = document.createElement('canvas');
            blank.width = canvas.width;
            blank.height = canvas.height;
            if (canvas.toDataURL() === blank.toDataURL()) {
                e.preventDefault();
                alert('Please provide your signature before submitting.');
                return;
            }
            document.getElementById('signatureData').value = canvas.toDataURL('image/png');
        });
    }
</script>
</body>
</html>
