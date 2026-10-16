package com.leaseapp.model.homeowner;

// Section 9 - Email address for service of documents by tenant on landlord
// (This is the LANDLORD's email address, provided by the homeowner,
//  used by the tenant to serve documents/notices on the landlord.)
public class LandlordServiceEmail {
    private String emailAddress;

    public LandlordServiceEmail() {}

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
}