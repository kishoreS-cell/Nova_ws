package com.leaseapp.model.homeowner;

// Section 1 (Landlord part) - filled by homeowner
public class LandlordInfo {
    private String name;
    private String streetAddress;
    private String mailingAddress;
    private String apartment;
    private String city;
    private String postalCode;
    private String phoneHome;
    private String phoneBusiness;

    public LandlordInfo() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getMailingAddress() { return mailingAddress; }
    public void setMailingAddress(String mailingAddress) { this.mailingAddress = mailingAddress; }

    public String getApartment() { return apartment; }
    public void setApartment(String apartment) { this.apartment = apartment; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhoneHome() { return phoneHome; }
    public void setPhoneHome(String phoneHome) { this.phoneHome = phoneHome; }

    public String getPhoneBusiness() { return phoneBusiness; }
    public void setPhoneBusiness(String phoneBusiness) { this.phoneBusiness = phoneBusiness; }
}