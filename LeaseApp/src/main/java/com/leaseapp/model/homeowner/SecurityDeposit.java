package com.leaseapp.model.homeowner;

import java.math.BigDecimal;

// Section 18 - Security deposit - filled by homeowner
public class SecurityDeposit {
    private boolean required;
    private BigDecimal amount;
    private String institution;

    public SecurityDeposit() {}

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
}