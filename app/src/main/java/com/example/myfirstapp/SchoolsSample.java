package com.example.myfirstapp;

public class SchoolsSample {
    private double longitude;
    private double latitude;
    private int osm_id;
    private String name;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getOsm_id() {
        return osm_id;
    }

    public void setOsm_id(int osm_id) {
        this.osm_id = osm_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SchoolsSample{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", osm_id=" + osm_id +
                ", name='" + name + '\'' +
                '}';
    }
}
