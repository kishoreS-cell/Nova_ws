package com.leaseapp.model.tenant;

// Section 26 - Tenants responsible for complying with terms and conditions (Attachments)
// Each tenant initials to confirm they received these documents.
public class AttachmentAcknowledgment {
    private boolean receivedAct;
    private boolean receivedSignedCopy;
    private boolean receivedRules;

    public AttachmentAcknowledgment() {}

    public boolean isReceivedAct() { return receivedAct; }
    public void setReceivedAct(boolean receivedAct) { this.receivedAct = receivedAct; }

    public boolean isReceivedSignedCopy() { return receivedSignedCopy; }
    public void setReceivedSignedCopy(boolean receivedSignedCopy) { this.receivedSignedCopy = receivedSignedCopy; }

    public boolean isReceivedRules() { return receivedRules; }
    public void setReceivedRules(boolean receivedRules) { this.receivedRules = receivedRules; }
}