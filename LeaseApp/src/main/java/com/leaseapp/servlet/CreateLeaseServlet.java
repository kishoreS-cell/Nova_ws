package com.leaseapp.servlet;

import com.leaseapp.dao.LeaseDAO;
import com.leaseapp.dao.TenantDAO;
import com.leaseapp.model.Lease;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/create-lease")
public class CreateLeaseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer homeownerId = (Integer) req.getSession().getAttribute("userId");
        if (homeownerId == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        // Edit mode: /create-lease?leaseId=5 loads the existing lease so the
        // homeowner can see and change their previously-saved data.
        String leaseIdParam = req.getParameter("leaseId");
        if (leaseIdParam != null && !leaseIdParam.isBlank()) {
            try {
                int leaseId = Integer.parseInt(leaseIdParam);
                LeaseDAO dao = new LeaseDAO();
                Lease lease = dao.getLeaseById(leaseId);

                // Ownership check - a homeowner can only edit their own lease
                if (lease == null || lease.getHomeownerId() != homeownerId) {
                    resp.sendRedirect("Dashboard.jsp");
                    return;
                }

                req.setAttribute("lease", lease);
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                resp.sendRedirect("Dashboard.jsp");
                return;
            }
        }

        req.getRequestDispatcher("create_lease.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer homeownerId = (Integer) session.getAttribute("userId");
        if (homeownerId == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        // If a leaseId came through (hidden field on the form), we're editing
        // an existing lease rather than creating a brand-new one.
        String leaseIdParam = req.getParameter("leaseId");
        boolean isEdit = leaseIdParam != null && !leaseIdParam.isBlank();
        int existingLeaseId = 0;

        if (isEdit) {
            try {
                existingLeaseId = Integer.parseInt(leaseIdParam);
            } catch (NumberFormatException e) {
                resp.sendRedirect("Dashboard.jsp");
                return;
            }
        }

        // ---------- Server-side validation (never trust the client alone) ----------
        List<String> errors = validate(req);
        if (!errors.isEmpty()) {
            req.setAttribute("error", String.join(" | ", errors));
            if (isEdit) {
                req.setAttribute("leaseId", leaseIdParam);
            }
            req.getRequestDispatcher("create_lease.jsp").forward(req, resp);
            return;
        }

        Lease lease = new Lease();
        lease.setHomeownerId(homeownerId);
        if (isEdit) {
            lease.setId(existingLeaseId);
        }

        // ---------- Section 1 (partial) - Landlord ----------
        lease.getLandlordInfo().setName(req.getParameter("landlordName"));
        lease.getLandlordInfo().setStreetAddress(req.getParameter("landlordStreetAddress"));
        lease.getLandlordInfo().setMailingAddress(req.getParameter("landlordMailingAddress"));
        lease.getLandlordInfo().setApartment(req.getParameter("landlordApartment"));
        lease.getLandlordInfo().setCity(req.getParameter("landlordCity"));
        lease.getLandlordInfo().setPostalCode(req.getParameter("landlordPostalCode"));
        lease.getLandlordInfo().setPhoneHome(req.getParameter("landlordPhoneHome"));
        lease.getLandlordInfo().setPhoneBusiness(req.getParameter("landlordPhoneBusiness"));

        // ---------- Section 3 - Premises ----------
        lease.getPremisesInfo().setStreetAddress(req.getParameter("premisesStreetAddress"));
        lease.getPremisesInfo().setApartment(req.getParameter("premisesApartment"));
        lease.getPremisesInfo().setCity(req.getParameter("premisesCity"));
        lease.getPremisesInfo().setPostalCode(req.getParameter("premisesPostalCode"));
        lease.getPremisesInfo().setPropertyType(req.getParameter("propertyType"));
        lease.getPremisesInfo().setTenantMailingPobox(req.getParameter("tenantMailingPobox"));
        lease.getPremisesInfo().setTenantMailingPostalCode(req.getParameter("tenantMailingPostalCode"));
        lease.getPremisesInfo().setTenantPhoneHome(req.getParameter("tenantPhoneHome"));
        lease.getPremisesInfo().setTenantPhoneBusiness(req.getParameter("tenantPhoneBusiness"));

        // ---------- Section 9 - Landlord's email for service ----------
        lease.getLandlordServiceEmail().setEmailAddress(req.getParameter("landlordServiceEmail"));

        // ---------- Section 11A/11B - Lease type ----------
        String leaseType = req.getParameter("leaseType"); // "PERIODIC" or "FIXED_TERM"
        lease.getLeaseTerm().setLeaseType(leaseType);

        if ("PERIODIC".equals(leaseType)) {
            lease.getLeaseTerm().setPeriodicStartDate(parseDate(req.getParameter("periodicStartDate")));
            lease.getLeaseTerm().setPeriodicTerm(req.getParameter("periodicTerm"));
        } else if ("FIXED_TERM".equals(leaseType)) {
            lease.getLeaseTerm().setFixedStartDate(parseDate(req.getParameter("fixedStartDate")));
            lease.getLeaseTerm().setFixedEndDate(parseDate(req.getParameter("fixedEndDate")));
        }

        // ---------- Section 13 - Rent ----------
        lease.getRentDetails().setAmount(parseBigDecimal(req.getParameter("rentAmount")));
        lease.getRentDetails().setFrequency(req.getParameter("rentFrequency"));
        lease.getRentDetails().setPaymentMethod(req.getParameter("paymentMethod"));
        lease.getRentDetails().setPaymentMethodOther(req.getParameter("paymentMethodOther"));
        lease.getRentDetails().setDueDay(parseInt(req.getParameter("rentDueDay")));
        lease.getRentDetails().setPayableTo(req.getParameter("rentPayableTo"));

        // ---------- Section 16 (top) - Rent includes ----------
        lease.getRentIncludes().setStove(req.getParameter("inclStove") != null);
        lease.getRentIncludes().setFridge(req.getParameter("inclFridge") != null);
        lease.getRentIncludes().setWasherDryer(req.getParameter("inclWasherDryer") != null);
        lease.getRentIncludes().setDishwasher(req.getParameter("inclDishwasher") != null);
        lease.getRentIncludes().setFurniture(req.getParameter("inclFurniture") != null);

        lease.getRentIncludes().setCoinLaundry(req.getParameter("inclCoinLaundry") != null);
        lease.getRentIncludes().setCable(req.getParameter("inclCable") != null);
        lease.getRentIncludes().setHeat(req.getParameter("inclHeat") != null);
        lease.getRentIncludes().setWater(req.getParameter("inclWater") != null);
        lease.getRentIncludes().setHotWater(req.getParameter("inclHotWater") != null);
        lease.getRentIncludes().setElectricity(req.getParameter("inclElectricity") != null);
        lease.getRentIncludes().setParkingSpaces(parseInt(req.getParameter("inclParkingSpaces")));
        lease.getRentIncludes().setParkingSpaceNumber(req.getParameter("inclParkingSpaceNumber"));
        lease.getRentIncludes().setRecyclingFacilities(req.getParameter("inclRecyclingFacilities") != null);

        lease.getRentIncludes().setLawnCare(req.getParameter("inclLawnCare") != null);
        lease.getRentIncludes().setSnowRemoval(req.getParameter("inclSnowRemoval") != null);
        lease.getRentIncludes().setGarbageRemoval(req.getParameter("inclGarbageRemoval") != null);
        lease.getRentIncludes().setOtherCustom(req.getParameter("inclOtherCustom"));

        // ---------- Section 16 (bottom) - Tenant responsibilities ----------
        lease.getTenantResponsibilities().setLawnCare(req.getParameter("respLawnCare") != null);
        lease.getTenantResponsibilities().setLatePayment(req.getParameter("respLatePayment") != null);
        lease.getTenantResponsibilities().setSnowRemoval(req.getParameter("respSnowRemoval") != null);
        lease.getTenantResponsibilities().setReturnedChequeLimit(parseBigDecimal(req.getParameter("respReturnedChequeLimit")));
        lease.getTenantResponsibilities().setGarbageRemoval(req.getParameter("respGarbageRemoval") != null);
        lease.getTenantResponsibilities().setParkingFee(parseBigDecimal(req.getParameter("respParkingFee")));
        lease.getTenantResponsibilities().setParkingSpaces(parseInt(req.getParameter("respParkingSpaces")));
        lease.getTenantResponsibilities().setTenantInsurance(req.getParameter("respTenantInsurance") != null);
        lease.getTenantResponsibilities().setLockoutChargeLimit(parseBigDecimal(req.getParameter("respLockoutChargeLimit")));
        lease.getTenantResponsibilities().setSeparationRecyclables(req.getParameter("respSeparationRecyclables") != null);
        lease.getTenantResponsibilities().setSubletExpenseLimit(parseBigDecimal(req.getParameter("respSubletExpenseLimit")));

        // ---------- Section 17 - Additional obligations ----------
        lease.setAdditionalObligations(req.getParameter("additionalObligations"));

        // ---------- Section 18 - Security deposit ----------
        boolean depositRequired = req.getParameter("securityDepositRequired") != null;
        lease.getSecurityDeposit().setRequired(depositRequired);
        if (depositRequired) {
            lease.getSecurityDeposit().setAmount(parseBigDecimal(req.getParameter("securityDepositAmount")));
            lease.getSecurityDeposit().setInstitution(req.getParameter("securityDepositInstitution"));
        }

        // ---------- Section 19 - Inspection ----------
        lease.setInspectionReportAttached(req.getParameter("inspectionReportAttached") != null);

        // Section 23 (Tenant's notice to quit) is fixed statutory text - no input needed.

        LeaseDAO dao = new LeaseDAO();
        try {
            if (isEdit) {
                // Only allow editing while still a draft - once tenants have
                // been invited, changing terms underneath a signature-in-progress
                // lease would be unsafe.
                Lease existing = dao.getLeaseById(existingLeaseId);
                if (existing == null || existing.getHomeownerId() != homeownerId) {
                    resp.sendRedirect("Dashboard.jsp");
                    return;
                }
                if (!"DRAFT".equals(existing.getStatus())) {
                    // Not a draft anymore, but that's only a hard stop once a tenant
                    // has actually signed. Before that, the homeowner can still fix
                    // mistakes even after sending it out.
                    TenantDAO tenantDAO = new TenantDAO();
                    boolean tenantAlreadySigned = tenantDAO.anyTenantSigned(existingLeaseId);
                    if (tenantAlreadySigned || !"PENDING_TENANT_SIGNATURES".equals(existing.getStatus())) {
                        req.setAttribute("error", "This lease can no longer be edited because a tenant has already signed.");
                        req.setAttribute("leaseId", leaseIdParam);
                        req.getRequestDispatcher("create_lease.jsp").forward(req, resp);
                        return;
                    }
                }

                dao.updateLease(lease);
                resp.sendRedirect("add-tenants?leaseId=" + existingLeaseId);
            } else {
                lease.setStatus("DRAFT");
                int leaseId = dao.createLease(lease);
                // Next step: adding tenants and sending the secure link (separate servlet)
                resp.sendRedirect("add-tenants?leaseId=" + leaseId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Could not save lease: " + e.getMessage());
            if (isEdit) {
                req.setAttribute("leaseId", leaseIdParam);
            }
            req.getRequestDispatcher("create_lease.jsp").forward(req, resp);
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        return Date.valueOf(value); // expects yyyy-MM-dd (HTML <input type="date">)
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Checks every field marked "required" in create_lease.jsp on the server side too.
    // Client-side HTML `required` can be bypassed (JS disabled, direct POST, etc.),
    // so this is the real enforcement.
    private List<String> validate(HttpServletRequest req) {
        List<String> errors = new ArrayList<>();

        // Section 1 - Landlord
        requireText(req, "landlordName", "Landlord name", errors);
        requireText(req, "landlordStreetAddress", "Landlord street address", errors);
        requireText(req, "landlordCity", "Landlord city", errors);
        requireText(req, "landlordPostalCode", "Landlord postal code", errors);

        // Section 3 - Premises
        requireText(req, "premisesStreetAddress", "Premises street address", errors);
        requireText(req, "premisesCity", "Premises city", errors);
        requireText(req, "premisesPostalCode", "Premises postal code", errors);
        requireText(req, "propertyType", "Property type", errors);

        // Section 11A/11B - Lease type (radio button - must be one of the two)
        String leaseType = req.getParameter("leaseType");
        if (leaseType == null || leaseType.isBlank()) {
            errors.add("Lease type (Periodic or Fixed-term) is required");
        } else if ("PERIODIC".equals(leaseType)) {
            requireText(req, "periodicStartDate", "Periodic lease start date", errors);
        } else if ("FIXED_TERM".equals(leaseType)) {
            requireText(req, "fixedStartDate", "Fixed-term start date", errors);
            requireText(req, "fixedEndDate", "Fixed-term end date", errors);
        }

        // Section 13 - Rent
        requireText(req, "rentAmount", "Rent amount", errors);
        String rentAmount = req.getParameter("rentAmount");
        if (rentAmount != null && !rentAmount.isBlank()) {
            try {
                if (new BigDecimal(rentAmount).compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Rent amount must be greater than 0");
                }
            } catch (NumberFormatException e) {
                errors.add("Rent amount must be a valid number");
            }
        }

        // Section 18 - Security deposit (conditionally required)
        if (req.getParameter("securityDepositRequired") != null) {
            requireText(req, "securityDepositAmount", "Security deposit amount", errors);
        }

        return errors;
    }

    private void requireText(HttpServletRequest req, String paramName, String label, List<String> errors) {
        String value = req.getParameter(paramName);
        if (value == null || value.isBlank()) {
            errors.add(label + " is required");
        }
    }
}