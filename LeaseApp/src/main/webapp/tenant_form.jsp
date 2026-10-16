<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.leaseapp.model.tenant.Tenant" %>
<%@ page import="com.leaseapp.model.Lease" %>
<%
    String linkError = (String) request.getAttribute("linkError");
    Boolean submittedSuccess = (Boolean) request.getAttribute("submittedSuccess");
    Tenant tenant = (Tenant) request.getAttribute("tenant");
    Lease lease = (Lease) request.getAttribute("lease");
    String token = request.getParameter("token");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Complete Your Lease Application</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 30px; }
        .container { max-width: 700px; margin: 0 auto; background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h2 { text-align: center; color: #1e3a8a; }
        p.subtitle { text-align: center; color: #6b7280; margin-top: -10px; }
        fieldset { border: 1px solid #d1d5db; border-radius: 6px; margin-bottom: 18px; padding: 15px 20px; }
        legend { font-weight: bold; color: #1d4ed8; padding: 0 8px; }
        label { display: block; margin-top: 10px; margin-bottom: 4px; font-size: 14px; color: #374151; }
        input[type=text], input[type=email], input[type=number] {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; font-size: 14px;
        }
        .row { display: flex; gap: 15px; }
        .row > div { flex: 1; }
        .checkbox-line label { font-weight: normal; display: flex; align-items: flex-start; gap: 8px; margin-top: 10px; }
        .checkbox-line input { width: auto; margin-top: 3px; }
        .occupant-row { display: flex; gap: 10px; margin-top: 8px; }
        .occupant-row input[type=text] { flex: 3; }
        .occupant-row input[type=number] { flex: 1; }
        button, .btn { padding: 12px; background: #2563eb; color: white; border: none; border-radius: 4px; font-size: 15px; cursor: pointer; }
        button:hover { background: #1d4ed8; }
        .btn-secondary { background: #6b7280; padding: 8px 14px; font-size: 13px; }
        .error { color: red; text-align: center; }
        .info-box { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 12px 16px; margin-bottom: 20px; font-size: 14px; color: #1e40af; }
        .success-box { background: #d1fae5; border: 1px solid #6ee7b7; border-radius: 6px; padding: 20px; text-align: center; color: #065f46; }
        canvas { border: 1px dashed #9ca3af; border-radius: 4px; touch-action: none; cursor: crosshair; background: #fff; }
        .sig-actions { margin-top: 8px; display: flex; gap: 10px; }
    </style>
</head>
<body>
<div class="container">

    <% if (linkError != null) { %>
        <h2>Complete Your Lease Application</h2>
        <p class="error"><%= linkError %></p>

    <% } else if (submittedSuccess != null && submittedSuccess) { %>
        <div class="success-box">
            <h2 style="color:#065f46;">Thank you, <%= tenant.getFullName() %>!</h2>
            <p>Your section has been submitted and signed successfully.<br>
            The homeowner will be notified once everyone has completed their part.</p>
        </div>

    <% } else { %>

        <h2>Complete Your Lease Application</h2>
        <p class="subtitle">Hi <%= tenant.getFullName() %>, please complete the sections below.</p>

        <div class="info-box">
            Property: <%= lease.getPremisesInfo().getStreetAddress() %>,
            <%= lease.getPremisesInfo().getCity() %>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>

        <form id="tenantForm" action="tenant-form" method="post">
            <input type="hidden" name="token" value="<%= token %>">
            <input type="hidden" name="signatureData" id="signatureData">

            <!-- Section 2 - Occupants -->
            <fieldset>
                <legend>Section 2 &mdash; Other Occupants (under 18 only)</legend>
                <p style="font-size:13px;color:#6b7280;">
                    List any other people (besides signing tenants) who will live at the property.
                    Anyone 18 or older must be added as a tenant by the homeowner, not listed here.
                </p>
                <div id="occupantRows">
                    <div class="occupant-row">
                        <input type="text" name="occupantName" placeholder="Full name">
                        <input type="number" name="occupantAge" placeholder="Age" min="0" max="17">
                    </div>
                </div>
                <button type="button" class="btn-secondary" onclick="addOccupantRow()">+ Add another</button>
            </fieldset>

            <!-- Section 4 - Emergency contact -->
            <fieldset>
                <legend>Section 4 &mdash; Emergency Contact</legend>
                <label>Next of Kin (Name)</label>
                <input type="text" name="emergencyNextOfKin" required>
                <div class="row">
                    <div>
                        <label>Phone (work)</label>
                        <input type="text" name="emergencyPhoneWork">
                    </div>
                    <div>
                        <label>Phone (home)</label>
                        <input type="text" name="emergencyPhoneHome">
                    </div>
                </div>
            </fieldset>

            <!-- Section 8 - Email for service -->
            <fieldset>
                <legend>Section 8 &mdash; Email for Service of Documents</legend>
                <label>Your Email Address (to receive documents from the landlord)</label>
                <input type="email" name="emailForService" value="<%= tenant.getEmail() %>">
            </fieldset>

            <!-- Section 26 - Attachments acknowledgment -->
            <fieldset>
                <legend>Section 26 &mdash; Acknowledgment</legend>
                <div class="checkbox-line">
                    <label><input type="checkbox" name="ackReceivedAct" required> I have received a copy of the Residential Tenancies Act.</label>
                </div>
                <div class="checkbox-line">
                    <label><input type="checkbox" name="ackReceivedSignedCopy" required> I will receive a copy of the signed lease within 10 days of signing.</label>
                </div>
                <div class="checkbox-line">
                    <label><input type="checkbox" name="ackReceivedRules" required> I have read and received the rules and attachments to this lease.</label>
                </div>
            </fieldset>

            <!-- Signature -->
            <fieldset>
                <legend>Signature</legend>
                <p style="font-size:13px;color:#6b7280;">Draw your signature below using your mouse or finger.</p>
                <canvas id="sigCanvas" width="600" height="150"></canvas>
                <div class="sig-actions">
                    <button type="button" class="btn-secondary" onclick="clearSignature()">Clear</button>
                </div>
            </fieldset>

            <button type="submit" style="width:100%;">Submit and Sign</button>
        </form>

    <% } %>
</div>

<script>
    // ---- Occupant rows (add more as needed) ----
    function addOccupantRow() {
        var container = document.getElementById('occupantRows');
        var row = document.createElement('div');
        row.className = 'occupant-row';
        row.innerHTML = '<input type="text" name="occupantName" placeholder="Full name">' +
                         '<input type="number" name="occupantAge" placeholder="Age" min="0" max="17">';
        container.appendChild(row);
    }

    // ---- Signature capture ----
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

        function stopDraw() {
            drawing = false;
        }

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

        document.getElementById('tenantForm').addEventListener('submit', function(e) {
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
