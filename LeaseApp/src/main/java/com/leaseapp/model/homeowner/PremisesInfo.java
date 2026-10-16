package com.leaseapp.model.homeowner;

// Section 3 - Premises (the rented property) - filled by homeowner
public class PremisesInfo {
    private String streetAddress;
    private String apartment;
    private String city;
    private String postalCode;
    private String propertyType;
    private String tenantMailingPobox;
    private String tenantMailingPostalCode;
    private String tenantPhoneHome;
    private String tenantPhoneBusiness;

    public PremisesInfo() {}

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getApartment() { return apartment; }
    public void setApartment(String apartment) { this.apartment = apartment; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public String getTenantMailingPobox() { return tenantMailingPobox; }
    public void setTenantMailingPobox(String tenantMailingPobox) { this.tenantMailingPobox = tenantMailingPobox; }

    public String getTenantMailingPostalCode() { return tenantMailingPostalCode; }
    public void setTenantMailingPostalCode(String tenantMailingPostalCode) { this.tenantMailingPostalCode = tenantMailingPostalCode; }

    public String getTenantPhoneHome() { return tenantPhoneHome; }
    public void setTenantPhoneHome(String tenantPhoneHome) { this.tenantPhoneHome = tenantPhoneHome; }

    public String getTenantPhoneBusiness() { return tenantPhoneBusiness; }
    public void setTenantPhoneBusiness(String tenantPhoneBusiness) { this.tenantPhoneBusiness = tenantPhoneBusiness; }
}