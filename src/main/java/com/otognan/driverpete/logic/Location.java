package com.otognan.driverpete.logic;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.persistence.Embeddable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

@Embeddable
public class Location {
    
    // the UTC time of this location, in milliseconds since January 1, 1970.
    private long time;
    // latitude, in degrees.
    private double latitude;
    // longitude, in degrees.
    private double longitude;
    
    public static double msToMph = 2.23694;
    
    public Location(long time, double latitude, double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public Location(double latitude, double longitude) {
        this(0, latitude, longitude);
    }
    
    public Location() {
        this(0, 0., 0.);
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
    // "04-08-2015_14-35-50_PDT 32.936004 -117.235370"
    // So far we ignore timezones and force timezone to be Pacific Daylight Time
    public static Location fromString(String str) throws ParseException {
        String[] parts = str.split(" ");
        Date date = Location.dateFromString(parts[0]);
        return new Location(date.getTime(), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
    
    public static Date dateFromString(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss_z");
        return formatter.parse(dateString);
    }
    
    // returns string to put into trajectory serialization file
    public String toSerializationString(String timezone) throws ParseException {
        return String.format("%s %f %f",
                Location.dateToString(this.getTime(), timezone),
                this.getLatitude(),
                this.getLongitude());
    }
    
    public static String dateToString(long epoch, String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss_z", Locale.US);
        if (timezone != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return sdf.format(epoch);
    }
    
    public static String dateToString(long epoch) {
        return Location.dateToString(epoch, null);
    }

    public String toSerializationString() throws Exception {
        return this.toSerializationString(null);
    }

    @Override
    public String toString() {
        String dateStr = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss_z", Locale.US).format(this.getTime());
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
    
    public static double distance(Location from, Location to) {
        float[] result = {0f};
        computeDistanceAndBearing(from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude(), result);
        return result[0];
    }
    
    public static double velocity(Location from, Location to) {
        return Location.distance(from, to)/Location.deltaTime(from, to);
    }
    
    public static double velocityMph(Location from, Location to) {
        return Location.velocity(from, to)*Location.msToMph;
    }
    
    private static void computeDistanceAndBearing(double lat1, double lon1,
            double lat2, double lon2, float[] results) {
        // copypaste from android.location

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda
                    / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2
                    / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1
                    + (uSquared / 16384.0)
                    * // (3)
                    (4096.0 + uSquared
                            * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared
                            * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha
                    * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B
                    * sinSigma
                    * // (6)
                    (cos2SM + (B / 4.0)
                            * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0)
                                    * cos2SM
                                    * (-3.0 + 4.0 * sinSigma * sinSigma)
                                    * (-3.0 + 4.0 * cos2SMSq)));

            lambda = L
                    + (1.0 - C)
                    * f
                    * sinAlpha
                    * (sigma + C
                            * sinSigma
                            * (cos2SM + C * cosSigma
                                    * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1
                    * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= 180.0 / Math.PI;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                        -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= 180.0 / Math.PI;
                results[2] = finalBearing;
            }
        }
    }
    
    public String getAddress() {

        HttpGet httpGet = new HttpGet(
                "http://maps.google.com/maps/api/geocode/json?latlng="+this.getLatitude()+","+this.getLongitude()+"&sensor=false");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        
        HttpEntity entity = response.getEntity();
        InputStream stream;
        try {
            stream = entity.getContent();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            return "";
        }
        
        int b;
        try {
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        //return stringBuilder.toString();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
            //Get JSON Array called "results" and then get the 0th complete object as JSON        
            JSONObject location = jsonObject.getJSONArray("results").getJSONObject(0); 
            // Get the value of the attribute whose name is "formatted_string"
            return location.getString("formatted_address");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
