package com.example.myfirstapp;

public class RegisteredParents {
    private String parentName;
    private String userLocation;
    private Integer schoolId;
    private String timeRegistration;

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Integer getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        this.schoolId = schoolId;
    }

    public void setTimeRegistration(Long timeRegistration) {
        this.timeRegistration = String.format("%1$td, %1$tb, %1$tY, %1$tR", timeRegistration);
    }

    public String getTimeRegistration() {
        return timeRegistration;
    }
}
