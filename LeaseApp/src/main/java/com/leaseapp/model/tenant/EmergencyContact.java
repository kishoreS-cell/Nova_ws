package com.leaseapp.model.tenant;

// Section 4 - Emergency contact
public class EmergencyContact {
    private String nextOfKin;   // name
    private String phoneWork;
    private String phoneHome;

    public EmergencyContact() {}

    public String getNextOfKin() { return nextOfKin; }
    public void setNextOfKin(String nextOfKin) { this.nextOfKin = nextOfKin; }

    public String getPhoneWork() { return phoneWork; }
    public void setPhoneWork(String phoneWork) { this.phoneWork = phoneWork; }

    public String getPhoneHome() { return phoneHome; }
    public void setPhoneHome(String phoneHome) { this.phoneHome = phoneHome; }
}