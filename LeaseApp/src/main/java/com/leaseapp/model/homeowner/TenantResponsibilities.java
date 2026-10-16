package com.leaseapp.model.homeowner;

import java.math.BigDecimal;

// Section 16 (bottom part) - what the tenant is responsible for, filled by homeowner
public class TenantResponsibilities {
    private boolean lawnCare;
    private boolean latePayment;
    private boolean snowRemoval;
    private BigDecimal returnedChequeLimit;
    private boolean garbageRemoval;
    private BigDecimal parkingFee;
    private int parkingSpaces;
    private boolean tenantInsurance;
    private BigDecimal lockoutChargeLimit;
    private boolean separationRecyclables;
    private BigDecimal subletExpenseLimit;

    public TenantResponsibilities() {}

    public boolean isLawnCare() { return lawnCare; }
    public void setLawnCare(boolean lawnCare) { this.lawnCare = lawnCare; }

    public boolean isLatePayment() { return latePayment; }
    public void setLatePayment(boolean latePayment) { this.latePayment = latePayment; }

    public boolean isSnowRemoval() { return snowRemoval; }
    public void setSnowRemoval(boolean snowRemoval) { this.snowRemoval = snowRemoval; }

    public BigDecimal getReturnedChequeLimit() { return returnedChequeLimit; }
    public void setReturnedChequeLimit(BigDecimal returnedChequeLimit) { this.returnedChequeLimit = returnedChequeLimit; }

    public boolean isGarbageRemoval() { return garbageRemoval; }
    public void setGarbageRemoval(boolean garbageRemoval) { this.garbageRemoval = garbageRemoval; }

    public BigDecimal getParkingFee() { return parkingFee; }
    public void setParkingFee(BigDecimal parkingFee) { this.parkingFee = parkingFee; }

    public int getParkingSpaces() { return parkingSpaces; }
    public void setParkingSpaces(int parkingSpaces) { this.parkingSpaces = parkingSpaces; }

    public boolean isTenantInsurance() { return tenantInsurance; }
    public void setTenantInsurance(boolean tenantInsurance) { this.tenantInsurance = tenantInsurance; }

    public BigDecimal getLockoutChargeLimit() { return lockoutChargeLimit; }
    public void setLockoutChargeLimit(BigDecimal lockoutChargeLimit) { this.lockoutChargeLimit = lockoutChargeLimit; }

    public boolean isSeparationRecyclables() { return separationRecyclables; }
    public void setSeparationRecyclables(boolean separationRecyclables) { this.separationRecyclables = separationRecyclables; }

    public BigDecimal getSubletExpenseLimit() { return subletExpenseLimit; }
    public void setSubletExpenseLimit(BigDecimal subletExpenseLimit) { this.subletExpenseLimit = subletExpenseLimit; }
}