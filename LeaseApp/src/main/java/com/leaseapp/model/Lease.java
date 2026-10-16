package com.leaseapp.model;

import java.sql.Date;
import java.sql.Timestamp;

import com.leaseapp.model.tenant.EmergencyContact;
import com.leaseapp.model.homeowner.LandlordInfo;
import com.leaseapp.model.homeowner.LandlordServiceEmail;
import com.leaseapp.model.homeowner.LeaseTerm;
import com.leaseapp.model.homeowner.PremisesInfo;
import com.leaseapp.model.homeowner.RentDetails;
import com.leaseapp.model.homeowner.RentIncludes;
import com.leaseapp.model.homeowner.SecurityDeposit;
import com.leaseapp.model.homeowner.TenantResponsibilities;

public class Lease {
    private int id;
    private int homeownerId;

    // Section 1 (partial) - Landlord
    private LandlordInfo landlordInfo = new LandlordInfo();

    // Section 3 - Premises
    private PremisesInfo premisesInfo = new PremisesInfo();

    // Section 4 - Emergency contact
    private EmergencyContact emergencyContact = new EmergencyContact();

    // Section 9 - Landlord's email for service of documents by tenant on landlord
    private LandlordServiceEmail landlordServiceEmail = new LandlordServiceEmail();

    // Section 11A/11B - Lease type
    private LeaseTerm leaseTerm = new LeaseTerm();

    // Section 13 - Rent
    private RentDetails rentDetails = new RentDetails();

    // Section 16 (top) - What rent includes
    private RentIncludes rentIncludes = new RentIncludes();

    // Section 16 (bottom) - Tenant responsibilities
    private TenantResponsibilities tenantResponsibilities = new TenantResponsibilities();

    // Section 17 - Additional obligations
    private String additionalObligations;

    // Section 18 - Security deposit
    private SecurityDeposit securityDeposit = new SecurityDeposit();

    // Section 19 - Inspection
    private boolean inspectionReportAttached;

    // Section 20 - Statutory conditions / building rules
    private boolean rulesAttached;
    private String rulesScheduleRef;

    // Status tracking (state machine)
    private String status;             // DRAFT / PENDING_TENANT_SIGNATURES / PENDING_HOMEOWNER_SIGNATURE / COMPLETED
    private String homeownerSignatureData;
    private Date homeownerSignedDate;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Lease() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHomeownerId() { return homeownerId; }
    public void setHomeownerId(int homeownerId) { this.homeownerId = homeownerId; }

    public LandlordInfo getLandlordInfo() { return landlordInfo; }
    public void setLandlordInfo(LandlordInfo landlordInfo) { this.landlordInfo = landlordInfo; }

    public PremisesInfo getPremisesInfo() { return premisesInfo; }
    public void setPremisesInfo(PremisesInfo premisesInfo) { this.premisesInfo = premisesInfo; }

    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(EmergencyContact emergencyContact) { this.emergencyContact = emergencyContact; }

    public LandlordServiceEmail getLandlordServiceEmail() { return landlordServiceEmail; }
    public void setLandlordServiceEmail(LandlordServiceEmail landlordServiceEmail) { this.landlordServiceEmail = landlordServiceEmail; }

    public LeaseTerm getLeaseTerm() { return leaseTerm; }
    public void setLeaseTerm(LeaseTerm leaseTerm) { this.leaseTerm = leaseTerm; }

    public RentDetails getRentDetails() { return rentDetails; }
    public void setRentDetails(RentDetails rentDetails) { this.rentDetails = rentDetails; }

    public RentIncludes getRentIncludes() { return rentIncludes; }
    public void setRentIncludes(RentIncludes rentIncludes) { this.rentIncludes = rentIncludes; }

    public TenantResponsibilities getTenantResponsibilities() { return tenantResponsibilities; }
    public void setTenantResponsibilities(TenantResponsibilities tenantResponsibilities) { this.tenantResponsibilities = tenantResponsibilities; }

    public String getAdditionalObligations() { return additionalObligations; }
    public void setAdditionalObligations(String additionalObligations) { this.additionalObligations = additionalObligations; }

    public SecurityDeposit getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(SecurityDeposit securityDeposit) { this.securityDeposit = securityDeposit; }

    public boolean isInspectionReportAttached() { return inspectionReportAttached; }
    public void setInspectionReportAttached(boolean inspectionReportAttached) { this.inspectionReportAttached = inspectionReportAttached; }

    public boolean isRulesAttached() { return rulesAttached; }
    public void setRulesAttached(boolean rulesAttached) { this.rulesAttached = rulesAttached; }

    public String getRulesScheduleRef() { return rulesScheduleRef; }
    public void setRulesScheduleRef(String rulesScheduleRef) { this.rulesScheduleRef = rulesScheduleRef; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHomeownerSignatureData() { return homeownerSignatureData; }
    public void setHomeownerSignatureData(String homeownerSignatureData) { this.homeownerSignatureData = homeownerSignatureData; }

    public Date getHomeownerSignedDate() { return homeownerSignedDate; }
    public void setHomeownerSignedDate(Date homeownerSignedDate) { this.homeownerSignedDate = homeownerSignedDate; }

    // No setters - DB-assigned/auto-updated timestamps
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}