package com.leaseapp.model.homeowner;

import java.sql.Date;

// Section 11A (Periodic lease) or 11B (Fixed-term lease) - filled by homeowner
public class LeaseTerm {
    private String leaseType;          // PERIODIC or FIXED_TERM

    // 11A - Periodic
    private Date periodicStartDate;
    private String periodicTerm;       // YEAR_TO_YEAR / MONTH_TO_MONTH / WEEK_TO_WEEK

    // 11B - Fixed-term
    private Date fixedStartDate;
    private Date fixedEndDate;

    public LeaseTerm() {}

    public String getLeaseType() { return leaseType; }
    public void setLeaseType(String leaseType) { this.leaseType = leaseType; }

    public Date getPeriodicStartDate() { return periodicStartDate; }
    public void setPeriodicStartDate(Date periodicStartDate) { this.periodicStartDate = periodicStartDate; }

    public String getPeriodicTerm() { return periodicTerm; }
    public void setPeriodicTerm(String periodicTerm) { this.periodicTerm = periodicTerm; }

    public Date getFixedStartDate() { return fixedStartDate; }
    public void setFixedStartDate(Date fixedStartDate) { this.fixedStartDate = fixedStartDate; }

    public Date getFixedEndDate() { return fixedEndDate; }
    public void setFixedEndDate(Date fixedEndDate) { this.fixedEndDate = fixedEndDate; }
}