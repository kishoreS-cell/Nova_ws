package com.leaseapp.model.tenant;

import java.sql.Date;
import java.sql.Timestamp;

public class Tenant {
    private int id;
    private int leaseId;
    private int tenantOrder;          // 1, 2, or 3

    // Section 1 (Tenant name)
    private String fullName;
    private String email;

    // Section 8 (Email address for service by landlord on tenant)
    private String emailForService;

    // Section 26 (Attachments acknowledgment - initials)
    private AttachmentAcknowledgment attachmentAcknowledgment = new AttachmentAcknowledgment();

    // Secure link
    private String secureToken;
    private Timestamp tokenExpiry;

    // Signature
    private boolean isSigned;
    private String signatureData;
    private Date signedDate;

    private Timestamp createdAt;

    public Tenant() {}

    public Tenant(int leaseId, int tenantOrder, String fullName, String email) {
        this.leaseId = leaseId;
        this.tenantOrder = tenantOrder;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLeaseId() { return leaseId; }
    public void setLeaseId(int leaseId) { this.leaseId = leaseId; }

    public int getTenantOrder() { return tenantOrder; }
    public void setTenantOrder(int tenantOrder) { this.tenantOrder = tenantOrder; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailForService() { return emailForService; }
    public void setEmailForService(String emailForService) { this.emailForService = emailForService; }

    public AttachmentAcknowledgment getAttachmentAcknowledgment() { return attachmentAcknowledgment; }
    public void setAttachmentAcknowledgment(AttachmentAcknowledgment attachmentAcknowledgment) { this.attachmentAcknowledgment = attachmentAcknowledgment; }

    public String getSecureToken() { return secureToken; }
    public void setSecureToken(String secureToken) { this.secureToken = secureToken; }

    public Timestamp getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(Timestamp tokenExpiry) { this.tokenExpiry = tokenExpiry; }

    public boolean isSigned() { return isSigned; }
    public void setSigned(boolean signed) { isSigned = signed; }

    public String getSignatureData() { return signatureData; }
    public void setSignatureData(String signatureData) { this.signatureData = signatureData; }

    public Date getSignedDate() { return signedDate; }
    public void setSignedDate(Date signedDate) { this.signedDate = signedDate; }

    // No setter - createdAt is auto-assigned by the DB
    public Timestamp getCreatedAt() { return createdAt; }
}