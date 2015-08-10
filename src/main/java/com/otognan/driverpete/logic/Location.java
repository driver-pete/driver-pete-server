package com.otognan.driverpete.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Location {
    
    // the UTC time of this location, in milliseconds since January 1, 1970.
    private long time;
    // latitude, in degrees.
    private double latitude;
    // longitude, in degrees.
    private double longitude;
   
    public Location(long time, double latitude, double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    
    // Create location from serialized trajectory string:
    // "04-08-2015_14-35-50 32.936004 -117.235370"
    public static Location fromString(String str) throws ParseException {
        String[] parts = str.split(" ");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Date date = formatter.parse(parts[0]);
        return new Location(date.getTime(), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
    
    // returns string to put into trajectory serialization file
    public String toSerializationString() {
        return String.format("%s %f %f",
                new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.US).format(this.getTime()),
                this.getLatitude(),
                this.getLongitude());
    }
}
