package com.example.myfirstapp;
/**
 * The SchoolsSample class is for creating a school object, then used for creating the schools list
 * obtained from the schoolsdata.csv
 */
public class SchoolsSample {
    private double longitude;
    private double latitude;
    private int osm_id;
    private String name;
    public double distance;

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


    /**
     * The haversine formula calculates the great-circle distance between two location points on a
     * sphere given the longitudes and latitudes.
     * @param longitude This is the first parameter to haversineFormula method.
     * @param latitude This is the second parameter to haversineFormula method.
     */
    public void haversineFormula(double longitude, double latitude) {
        this.distance = 6371*Math.acos(Math.cos(Math.toRadians(90-latitude))*
                Math.cos(Math.toRadians(90-this.latitude))+Math.sin(Math.toRadians(90-latitude))*
                Math.sin(Math.toRadians(90-this.latitude))*Math.cos(Math.toRadians(longitude-this.longitude)));
    }

}
