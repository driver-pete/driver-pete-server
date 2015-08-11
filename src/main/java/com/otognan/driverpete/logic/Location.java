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

    @Override
    public String toString() {
        String dateStr = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.US).format(this.getTime());
        return "Location [time=" + dateStr + ", latitude=" + latitude
                + ", longitude=" + longitude + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;
        if (time != other.time)
            return false;
        return true;
    }
    
    public static double deltaTime(Location from, Location to) {
        return (to.getTime() - from.getTime())/1000.;
    }
}
