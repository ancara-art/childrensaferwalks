package com.example.myfirstapp;

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

    @Override
    public String toString() {
        return "SchoolsSample{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", osm_id=" + osm_id +
                ", name='" + name + '\'' +
                '}';
    }

    // The haversine formula determines the great-circle distance between two points on a sphere
    // given the longitudes and latitudes.
    public void haversineFormula(double longitude, double latitude) {
/*        this.distance = Math.pow(Math.pow(this.longitude - longitude, 2) +
                                 Math.pow(this.latitude - latitude, 2), 0.5);*/

        this.distance = 6371*Math.acos(Math.cos(Math.toRadians(90-latitude))*
                Math.cos(Math.toRadians(90-this.latitude))+Math.sin(Math.toRadians(90-latitude))*
                Math.sin(Math.toRadians(90-this.latitude))*Math.cos(Math.toRadians(longitude-this.longitude)));
    }

}
