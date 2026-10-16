<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.leaseapp.model.Lease" %>
<%@ page import="com.leaseapp.model.homeowner.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }

    Lease lease = (Lease) request.getAttribute("lease");
    boolean isEdit = (lease != null);

    String leaseIdVal = "";
    if (isEdit) {
        leaseIdVal = String.valueOf(lease.getId());
    } else if (request.getAttribute("leaseId") != null) {
        // Re-showing the form after a validation/DB error while editing
        leaseIdVal = (String) request.getAttribute("leaseId");
        isEdit = true;
    }

    LandlordInfo l = isEdit && lease != null ? lease.getLandlordInfo() : new LandlordInfo();
    PremisesInfo p = isEdit && lease != null ? lease.getPremisesInfo() : new PremisesInfo();
    LandlordServiceEmail lse = isEdit && lease != null ? lease.getLandlordServiceEmail() : new LandlordServiceEmail();
    LeaseTerm lt = isEdit && lease != null ? lease.getLeaseTerm() : new LeaseTerm();
    RentDetails rd = isEdit && lease != null ? lease.getRentDetails() : new RentDetails();
    RentIncludes ri = isEdit && lease != null ? lease.getRentIncludes() : new RentIncludes();
    TenantResponsibilities tr = isEdit && lease != null ? lease.getTenantResponsibilities() : new TenantResponsibilities();
    SecurityDeposit sd = isEdit && lease != null ? lease.getSecurityDeposit() : new SecurityDeposit();
    String additionalObligations = isEdit && lease != null ? lease.getAdditionalObligations() : "";
    boolean inspectionAttached = isEdit && lease != null && lease.isInspectionReportAttached();

    // Small null-safety helper used everywhere below
    java.util.function.Function<String, String> nz = s -> (s == null ? "" : s);
%>
<!DOCTYPE html>
<html>
<head>
    <title><%= isEdit ? "Edit Lease" : "Create Lease" %> - Lease App</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 30px; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h2 { text-align: center; color: #1e3a8a; }
        fieldset { border: 1px solid #d1d5db; border-radius: 6px; margin-bottom: 20px; padding: 15px 20px; }
        legend { font-weight: bold; color: #1d4ed8; padding: 0 8px; }
        label { display: block; margin-top: 10px; margin-bottom: 4px; font-size: 14px; color: #374151; }
        input[type=text], input[type=number], input[type=date], input[type=email], select, textarea {
            width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; font-size: 14px;
        }
        .row { display: flex; gap: 15px; }
        .row > div { flex: 1; }
        .checkbox-group label { display: inline-flex; align-items: center; gap: 6px; font-weight: normal; margin-right: 15px; margin-top: 6px; }
        .checkbox-group input { width: auto; }
        .radio-group label { font-weight: normal; display: inline-block; margin-right: 20px; }
        .radio-group input { width: auto; margin-right: 6px; }
        button { width: 100%; padding: 12px; background: #2563eb; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; margin-top: 10px; }
        button:hover { background: #1d4ed8; }
        .error { color: red; text-align: center; }
        .conditional { display: none; margin-top: 10px; padding: 10px; background: #f9fafb; border-radius: 4px; }
    </style>
    <script>
        function toggleLeaseType() {
            var type = document.querySelector('input[name="leaseType"]:checked');
            document.getElementById('periodicFields').style.display = 'none';
            document.getElementById('fixedFields').style.display = 'none';
            if (type) {
                if (type.value === 'PERIODIC') document.getElementById('periodicFields').style.display = 'block';
                if (type.value === 'FIXED_TERM') document.getElementById('fixedFields').style.display = 'block';
            }
        }
        function toggleDeposit() {
            var checked = document.getElementById('securityDepositRequired').checked;
            document.getElementById('depositFields').style.display = checked ? 'block' : 'none';
        }
        function togglePaymentOther() {
            var val = document.getElementById('paymentMethod').value;
            document.getElementById('paymentOtherField').style.display = (val === 'OTHER') ? 'block' : 'none';
        }
        // Re-apply conditional visibility on load, so pre-filled edit data shows correctly
        window.onload = function() {
            toggleLeaseType();
            toggleDeposit();
            togglePaymentOther();
        };
    </script>
</head>
<body>
<div class="container">
    <h2><%= isEdit ? "Edit Lease" : "Create New Lease" %></h2>

    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>

    <form action="create-lease" method="post">
        <input type="hidden" name="leaseId" value="<%= leaseIdVal %>">

        <!-- Section 1 (partial) - Landlord -->
        <fieldset>
            <legend>Section 1 &mdash; Landlord</legend>
            <label>Landlord Name</label>
            <input type="text" name="landlordName" value="<%= nz.apply(l.getName()) %>" required>

            <label>Street Address</label>
            <input type="text" name="landlordStreetAddress" value="<%= nz.apply(l.getStreetAddress()) %>" required>

            <label>Mailing Address (if different)</label>
            <input type="text" name="landlordMailingAddress" value="<%= nz.apply(l.getMailingAddress()) %>">

            <div class="row">
                <div>
                    <label>Apartment</label>
                    <input type="text" name="landlordApartment" value="<%= nz.apply(l.getApartment()) %>">
                </div>
                <div>
                    <label>City / Town</label>
                    <input type="text" name="landlordCity" value="<%= nz.apply(l.getCity()) %>" required>
                </div>
                <div>
                    <label>Postal Code</label>
                    <input type="text" name="landlordPostalCode" value="<%= nz.apply(l.getPostalCode()) %>" required>
                </div>
            </div>

            <div class="row">
                <div>
                    <label>Phone (home)</label>
                    <input type="text" name="landlordPhoneHome" value="<%= nz.apply(l.getPhoneHome()) %>">
                </div>
                <div>
                    <label>Phone (business/other)</label>
                    <input type="text" name="landlordPhoneBusiness" value="<%= nz.apply(l.getPhoneBusiness()) %>">
                </div>
            </div>
        </fieldset>

        <!-- Section 3 - Premises -->
        <fieldset>
            <legend>Section 3 &mdash; Premises</legend>
            <label>Street Address of Rental Property</label>
            <input type="text" name="premisesStreetAddress" value="<%= nz.apply(p.getStreetAddress()) %>" required>

            <div class="row">
                <div>
                    <label>Apartment</label>
                    <input type="text" name="premisesApartment" value="<%= nz.apply(p.getApartment()) %>">
                </div>
                <div>
                    <label>City / Town</label>
                    <input type="text" name="premisesCity" value="<%= nz.apply(p.getCity()) %>" required>
                </div>
                <div>
                    <label>Postal Code</label>
                    <input type="text" name="premisesPostalCode" value="<%= nz.apply(p.getPostalCode()) %>" required>
                </div>
            </div>

            <label>Type of Property</label>
            <input type="text" name="propertyType" value="<%= nz.apply(p.getPropertyType()) %>" placeholder="e.g. Detached, Apartment" required>

            <div class="row">
                <div>
                    <label>Tenant's Mailing P.O. Box (if applicable)</label>
                    <input type="text" name="tenantMailingPobox" value="<%= nz.apply(p.getTenantMailingPobox()) %>">
                </div>
                <div>
                    <label>Postal Code</label>
                    <input type="text" name="tenantMailingPostalCode" value="<%= nz.apply(p.getTenantMailingPostalCode()) %>">
                </div>
            </div>

            <div class="row">
                <div>
                    <label>Tenant's Phone (home)</label>
                    <input type="text" name="tenantPhoneHome" value="<%= nz.apply(p.getTenantPhoneHome()) %>">
                </div>
                <div>
                    <label>Tenant's Phone (business/other)</label>
                    <input type="text" name="tenantPhoneBusiness" value="<%= nz.apply(p.getTenantPhoneBusiness()) %>">
                </div>
            </div>
        </fieldset>

        <!-- Section 9 - Landlord's email for service -->
        <fieldset>
            <legend>Section 9 &mdash; Email for Service of Documents (Tenant to Landlord)</legend>
            <label>Landlord's Email Address</label>
            <input type="email" name="landlordServiceEmail" value="<%= nz.apply(lse.getEmailAddress()) %>">
        </fieldset>

        <!-- Section 11A/11B - Lease type -->
        <fieldset>
            <legend>Section 11 &mdash; Lease Type</legend>
            <div class="radio-group">
                <label><input type="radio" name="leaseType" value="PERIODIC" onclick="toggleLeaseType()" <%= "PERIODIC".equals(lt.getLeaseType()) ? "checked" : "" %> required> Periodic Lease (11A)</label>
                <label><input type="radio" name="leaseType" value="FIXED_TERM" onclick="toggleLeaseType()" <%= "FIXED_TERM".equals(lt.getLeaseType()) ? "checked" : "" %>> Fixed-Term Lease (11B)</label>
            </div>

            <div id="periodicFields" class="conditional">
                <label>Tenancy Start Date</label>
                <input type="date" name="periodicStartDate" value="<%= lt.getPeriodicStartDate() != null ? lt.getPeriodicStartDate().toString() : "" %>">
                <label>Term</label>
                <select name="periodicTerm">
                    <option value="YEAR_TO_YEAR" <%= "YEAR_TO_YEAR".equals(lt.getPeriodicTerm()) ? "selected" : "" %>>Year-to-Year</option>
                    <option value="MONTH_TO_MONTH" <%= "MONTH_TO_MONTH".equals(lt.getPeriodicTerm()) ? "selected" : "" %>>Month-to-Month</option>
                    <option value="WEEK_TO_WEEK" <%= "WEEK_TO_WEEK".equals(lt.getPeriodicTerm()) ? "selected" : "" %>>Week-to-Week</option>
                </select>
            </div>

            <div id="fixedFields" class="conditional">
                <label>Start Date</label>
                <input type="date" name="fixedStartDate" value="<%= lt.getFixedStartDate() != null ? lt.getFixedStartDate().toString() : "" %>">
                <label>End Date</label>
                <input type="date" name="fixedEndDate" value="<%= lt.getFixedEndDate() != null ? lt.getFixedEndDate().toString() : "" %>">
            </div>
        </fieldset>

        <!-- Section 13 - Rent -->
        <fieldset>
            <legend>Section 13 &mdash; Rent</legend>
            <div class="row">
                <div>
                    <label>Rent Amount ($)</label>
                    <input type="number" step="0.01" name="rentAmount" value="<%= rd.getAmount() != null ? rd.getAmount().toString() : "" %>" required>
                </div>
                <div>
                    <label>Frequency</label>
                    <select name="rentFrequency">
                        <option value="MONTH" <%= !"WEEK".equals(rd.getFrequency()) ? "selected" : "" %>>Per Month</option>
                        <option value="WEEK" <%= "WEEK".equals(rd.getFrequency()) ? "selected" : "" %>>Per Week</option>
                    </select>
                </div>
            </div>

            <label>Payment Method</label>
            <select name="paymentMethod" id="paymentMethod" onchange="togglePaymentOther()">
                <option value="CASH" <%= "CASH".equals(rd.getPaymentMethod()) || rd.getPaymentMethod() == null ? "selected" : "" %>>Cash</option>
                <option value="AUTO_WITHDRAWAL" <%= "AUTO_WITHDRAWAL".equals(rd.getPaymentMethod()) ? "selected" : "" %>>Pre-authorized Automatic Withdrawal</option>
                <option value="POST_DATED_CHEQUES" <%= "POST_DATED_CHEQUES".equals(rd.getPaymentMethod()) ? "selected" : "" %>>Post-dated Cheques</option>
                <option value="CHEQUE" <%= "CHEQUE".equals(rd.getPaymentMethod()) ? "selected" : "" %>>Cheque</option>
                <option value="OTHER" <%= "OTHER".equals(rd.getPaymentMethod()) ? "selected" : "" %>>Other</option>
            </select>
            <div id="paymentOtherField" class="conditional">
                <label>Specify Other Payment Method</label>
                <input type="text" name="paymentMethodOther" value="<%= nz.apply(rd.getPaymentMethodOther()) %>">
            </div>

            <div class="row">
                <div>
                    <label>Rent Due Day (day of month/week)</label>
                    <input type="number" name="rentDueDay" min="1" max="31" value="<%= rd.getDueDay() > 0 ? String.valueOf(rd.getDueDay()) : "" %>">
                </div>
                <div>
                    <label>Rent Payable To</label>
                    <input type="text" name="rentPayableTo" value="<%= nz.apply(rd.getPayableTo()) %>">
                </div>
            </div>
        </fieldset>

        <!-- Section 16 (top) - Rent includes -->
        <fieldset>
            <legend>Section 16 &mdash; Rent Includes</legend>

            <label>Appliances</label>
            <div class="checkbox-group">
                <label><input type="checkbox" name="inclStove" <%= ri.isStove() ? "checked" : "" %>> Stove</label>
                <label><input type="checkbox" name="inclFridge" <%= ri.isFridge() ? "checked" : "" %>> Fridge</label>
                <label><input type="checkbox" name="inclWasherDryer" <%= ri.isWasherDryer() ? "checked" : "" %>> Washer &amp; Dryer</label>
                <label><input type="checkbox" name="inclDishwasher" <%= ri.isDishwasher() ? "checked" : "" %>> Dishwasher</label>
                <label><input type="checkbox" name="inclFurniture" <%= ri.isFurniture() ? "checked" : "" %>> Furniture</label>
            </div>

            <label>Utilities</label>
            <div class="checkbox-group">
                <label><input type="checkbox" name="inclCoinLaundry" <%= ri.isCoinLaundry() ? "checked" : "" %>> Washer/Dryer (coin operated)</label>
                <label><input type="checkbox" name="inclCable" <%= ri.isCable() ? "checked" : "" %>> Cable Service</label>
                <label><input type="checkbox" name="inclHeat" <%= ri.isHeat() ? "checked" : "" %>> Heat</label>
                <label><input type="checkbox" name="inclWater" <%= ri.isWater() ? "checked" : "" %>> Water</label>
                <label><input type="checkbox" name="inclHotWater" <%= ri.isHotWater() ? "checked" : "" %>> Hot Water</label>
                <label><input type="checkbox" name="inclElectricity" <%= ri.isElectricity() ? "checked" : "" %>> Electricity</label>
                <label><input type="checkbox" name="inclRecyclingFacilities" <%= ri.isRecyclingFacilities() ? "checked" : "" %>> Recycling Facilities</label>
            </div>
            <div class="row">
                <div>
                    <label># of Parking Spaces</label>
                    <input type="number" name="inclParkingSpaces" min="0" value="<%= ri.getParkingSpaces() > 0 ? String.valueOf(ri.getParkingSpaces()) : "" %>">
                </div>
                <div>
                    <label>Space #</label>
                    <input type="text" name="inclParkingSpaceNumber" value="<%= nz.apply(ri.getParkingSpaceNumber()) %>">
                </div>
            </div>

            <label>Other Included</label>
            <div class="checkbox-group">
                <label><input type="checkbox" name="inclLawnCare" <%= ri.isLawnCare() ? "checked" : "" %>> Lawn Care</label>
                <label><input type="checkbox" name="inclSnowRemoval" <%= ri.isSnowRemoval() ? "checked" : "" %>> Snow Removal</label>
                <label><input type="checkbox" name="inclGarbageRemoval" <%= ri.isGarbageRemoval() ? "checked" : "" %>> Garbage Removal</label>
            </div>
            <label>Other (specify)</label>
            <input type="text" name="inclOtherCustom" value="<%= nz.apply(ri.getOtherCustom()) %>">
        </fieldset>

        <!-- Section 16 (bottom) - Tenant responsibilities -->
        <fieldset>
            <legend>Section 16 &mdash; Tenant is Responsible For</legend>
            <div class="checkbox-group">
                <label><input type="checkbox" name="respLawnCare" <%= tr.isLawnCare() ? "checked" : "" %>> Lawn Care</label>
                <label><input type="checkbox" name="respLatePayment" <%= tr.isLatePayment() ? "checked" : "" %>> Late Payment Charges</label>
                <label><input type="checkbox" name="respSnowRemoval" <%= tr.isSnowRemoval() ? "checked" : "" %>> Snow Removal</label>
                <label><input type="checkbox" name="respGarbageRemoval" <%= tr.isGarbageRemoval() ? "checked" : "" %>> Garbage Removal</label>
                <label><input type="checkbox" name="respTenantInsurance" <%= tr.isTenantInsurance() ? "checked" : "" %>> Tenant Insurance</label>
                <label><input type="checkbox" name="respSeparationRecyclables" <%= tr.isSeparationRecyclables() ? "checked" : "" %>> Separation of Recyclables</label>
            </div>

            <div class="row">
                <div>
                    <label>Returned Cheque Charge Limit ($)</label>
                    <input type="number" step="0.01" name="respReturnedChequeLimit" value="<%= tr.getReturnedChequeLimit() != null ? tr.getReturnedChequeLimit().toString() : "" %>">
                </div>
                <div>
                    <label>Lockout Charge Limit ($)</label>
                    <input type="number" step="0.01" name="respLockoutChargeLimit" value="<%= tr.getLockoutChargeLimit() != null ? tr.getLockoutChargeLimit().toString() : "" %>">
                </div>
            </div>

            <div class="row">
                <div>
                    <label>Parking Fee ($/month)</label>
                    <input type="number" step="0.01" name="respParkingFee" value="<%= tr.getParkingFee() != null ? tr.getParkingFee().toString() : "" %>">
                </div>
                <div>
                    <label># of Parking Spaces</label>
                    <input type="number" name="respParkingSpaces" min="0" value="<%= tr.getParkingSpaces() > 0 ? String.valueOf(tr.getParkingSpaces()) : "" %>">
                </div>
            </div>

            <label>Sublet Expense Limit ($) &mdash; not to exceed $75</label>
            <input type="number" step="0.01" name="respSubletExpenseLimit" max="75" value="<%= tr.getSubletExpenseLimit() != null ? tr.getSubletExpenseLimit().toString() : "75" %>">
        </fieldset>

        <!-- Section 17 - Additional obligations -->
        <fieldset>
            <legend>Section 17 &mdash; Additional Obligations</legend>
            <textarea name="additionalObligations" rows="3" placeholder="Any additional terms..."><%= nz.apply(additionalObligations) %></textarea>
        </fieldset>

        <!-- Section 18 - Security deposit -->
        <fieldset>
            <legend>Section 18 &mdash; Security Deposit</legend>
            <label><input type="checkbox" id="securityDepositRequired" name="securityDepositRequired" onclick="toggleDeposit()" <%= sd.isRequired() ? "checked" : "" %>> Security deposit required</label>

            <div id="depositFields" class="conditional">
                <label>Deposit Amount ($) &mdash; not to exceed 1/2 month's rent</label>
                <input type="number" step="0.01" name="securityDepositAmount" value="<%= sd.getAmount() != null ? sd.getAmount().toString() : "" %>">
                <label>Financial Institution / Branch</label>
                <input type="text" name="securityDepositInstitution" value="<%= nz.apply(sd.getInstitution()) %>">
            </div>
        </fieldset>

        <!-- Section 19 - Inspection -->
        <fieldset>
            <legend>Section 19 &mdash; Inspection</legend>
            <label><input type="checkbox" name="inspectionReportAttached" <%= inspectionAttached ? "checked" : "" %>> Inspection report is attached to the lease</label>
        </fieldset>

        <button type="submit"><%= isEdit ? "Save Changes" : "Save and Continue to Add Tenants" %></button>
    </form>
</div>
</body>
</html>
