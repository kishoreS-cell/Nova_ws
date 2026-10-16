package com.leaseapp.model.homeowner;

// Section 16 (top part) - what the rent includes, filled by homeowner
public class RentIncludes {
    // Appliances
    private boolean stove;
    private boolean fridge;
    private boolean washerDryer;
    private boolean dishwasher;
    private boolean furniture;

    // Utilities
    private boolean coinLaundry;
    private boolean cable;
    private boolean heat;
    private boolean water;
    private boolean hotWater;
    private boolean electricity;
    private int parkingSpaces;
    private String parkingSpaceNumber;
    private boolean recyclingFacilities;

    // Other included
    private boolean lawnCare;
    private boolean snowRemoval;
    private boolean garbageRemoval;
    private String otherCustom;

    public RentIncludes() {}

    public boolean isStove() { return stove; }
    public void setStove(boolean stove) { this.stove = stove; }

    public boolean isFridge() { return fridge; }
    public void setFridge(boolean fridge) { this.fridge = fridge; }

    public boolean isWasherDryer() { return washerDryer; }
    public void setWasherDryer(boolean washerDryer) { this.washerDryer = washerDryer; }

    public boolean isDishwasher() { return dishwasher; }
    public void setDishwasher(boolean dishwasher) { this.dishwasher = dishwasher; }

    public boolean isFurniture() { return furniture; }
    public void setFurniture(boolean furniture) { this.furniture = furniture; }

    public boolean isCoinLaundry() { return coinLaundry; }
    public void setCoinLaundry(boolean coinLaundry) { this.coinLaundry = coinLaundry; }

    public boolean isCable() { return cable; }
    public void setCable(boolean cable) { this.cable = cable; }

    public boolean isHeat() { return heat; }
    public void setHeat(boolean heat) { this.heat = heat; }

    public boolean isWater() { return water; }
    public void setWater(boolean water) { this.water = water; }

    public boolean isHotWater() { return hotWater; }
    public void setHotWater(boolean hotWater) { this.hotWater = hotWater; }

    public boolean isElectricity() { return electricity; }
    public void setElectricity(boolean electricity) { this.electricity = electricity; }

    public int getParkingSpaces() { return parkingSpaces; }
    public void setParkingSpaces(int parkingSpaces) { this.parkingSpaces = parkingSpaces; }

    public String getParkingSpaceNumber() { return parkingSpaceNumber; }
    public void setParkingSpaceNumber(String parkingSpaceNumber) { this.parkingSpaceNumber = parkingSpaceNumber; }

    public boolean isRecyclingFacilities() { return recyclingFacilities; }
    public void setRecyclingFacilities(boolean recyclingFacilities) { this.recyclingFacilities = recyclingFacilities; }

    public boolean isLawnCare() { return lawnCare; }
    public void setLawnCare(boolean lawnCare) { this.lawnCare = lawnCare; }

    public boolean isSnowRemoval() { return snowRemoval; }
    public void setSnowRemoval(boolean snowRemoval) { this.snowRemoval = snowRemoval; }

    public boolean isGarbageRemoval() { return garbageRemoval; }
    public void setGarbageRemoval(boolean garbageRemoval) { this.garbageRemoval = garbageRemoval; }

    public String getOtherCustom() { return otherCustom; }
    public void setOtherCustom(String otherCustom) { this.otherCustom = otherCustom; }
}