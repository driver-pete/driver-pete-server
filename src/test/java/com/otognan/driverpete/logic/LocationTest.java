package com.otognan.driverpete.logic;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

public class LocationTest {

    @Test
    public void testLocationFromString() throws Exception {
        String locationString = "04-08-2015_14-35-50_PDT 32.936004 -117.235370";
        Location l = Location.fromString(locationString);

        assertEquals(1438724150000l, l.getTime());
        assertEquals(32.936004, l.getLatitude(), 1e-10);
        assertEquals(-117.235370, l.getLongitude(), 1e-10);
        
        assertEquals(locationString, l.toSerializationString("PST"));
    }

    @Test
    public void testLocationFromStringDifferentTimezon() throws ParseException {
        String locationString = "04-08-2015_14-35-50_MSK 32.936004 -117.235370";
        Location l = Location.fromString(locationString);

        assertEquals(1438688150000l, l.getTime());
        assertEquals(32.936004, l.getLatitude(), 1e-10);
        assertEquals(-117.235370, l.getLongitude(), 1e-10);
        
        assertEquals(locationString, l.toSerializationString("Europe/Moscow"));
    }

    @Test
    public void testDeltaTimer() throws ParseException {
        String [] locationsStr = {
         "04-08-2015_14-35-50_PDT 0 0",
         "04-08-2015_14-52-31_PDT 0 0",
         "04-08-2015_14-52-37_PDT 0 0",
         "04-08-2015_14-59-30_PDT 0 0",
         "04-08-2015_14-59-30_PDT 0 0",
         "05-08-2015_15-46-30_PDT 0 0",
         "05-09-2015_15-46-38_PDT 0 0",
        };
        
        ArrayList<Location> locations = new ArrayList<Location>();
        for (String el : locationsStr) {
            locations.add(Location.fromString(el));
        }
        
        double[] dt = new double[locations.size()-1];
        for (int i = 0; i < locations.size() - 1; i++) {
            dt[i] = Location.deltaTime(locations.get(i), locations.get(i+1));
        }
        
        double[] expecteddt = {1001, 6, 413, 0, 89220, 2678408};
        
        assertArrayEquals(dt, expecteddt, 1e-10);
    }

    @Test
    public void testDistance() throws ParseException {
        String [] locationsStr = {
         "04-08-2015_14-35-50_PDT 32.936004 -117.23537",
         "04-08-2015_14-52-31_PDT 32.934912 -117.236338",
         "04-08-2015_14-52-37_PDT 32.935667 -117.235796",
         "04-08-2015_14-59-30_PDT 32.935667 -117.235796",
         "04-08-2015_14-59-30_PDT 32.936034 -117.23537",
        };

        ArrayList<Location> locations = new ArrayList<Location>();
        for (String el : locationsStr) {
            locations.add(Location.fromString(el));
        }
        
        double[] ds = new double[locations.size()-1];
        for (int i = 0; i < locations.size() - 1; i++) {
            ds[i] = Location.distance(locations.get(i), locations.get(i+1));
        }
        
        // values from python
        double[] expectedds = {151.20243391843636, 97.87941457631524, 0.0, 56.95460850285275};
        
        assertArrayEquals(ds, expectedds, 1e-5);
    }
    
    @Test
    public void testGetAddress() throws Exception {
        String locationString = "04-08-2015_14-35-50_PDT 32.936004 -117.235370";
        Location l = Location.fromString(locationString);
        assertEquals(l.getAddress(), "3611 Valley Centre Drive, San Diego, CA 92130, USA");
    }
}
