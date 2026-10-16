package com.leaseapp.model.tenant;

// Section 2 - Occupants (other adults or children who will occupy the premises,
// but are not signing tenants - e.g. under 18 per the age rule)
public class Occupant {
    private int id;
    private int leaseId;
    private String fullName;
    private int age;

    public Occupant() {}

    public Occupant(String fullName, int age) {
        this.fullName = fullName;
        this.age = age;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLeaseId() { return leaseId; }
    public void setLeaseId(int leaseId) { this.leaseId = leaseId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}