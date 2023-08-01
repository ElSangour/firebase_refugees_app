package com.example.firebase_refugees_app.Utils;

public class LocationEntry {
    private double latitude;
    private double longitude;

    public LocationEntry() {
        // Default constructor required for Firebase
    }

    public LocationEntry(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}