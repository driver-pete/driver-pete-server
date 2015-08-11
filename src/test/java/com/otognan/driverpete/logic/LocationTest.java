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
    public void testLocationFromString() throws ParseException {
        String locationString = "04-08-2015_14-35-50 32.936004 -117.235370";
        Location l = Location.fromString(locationString);

        assertEquals(1438724150000l, l.getTime());
        assertEquals(32.936004, l.getLatitude(), 1e-10);
        assertEquals(-117.235370, l.getLongitude(), 1e-10);
        
        assertEquals(locationString, l.toSerializationString());
    }

    @Test
    public void testDeltaTimer() throws ParseException {
        String [] locationsStr = {
         "04-08-2015_14-35-50 0 0",
         "04-08-2015_14-52-31 0 0",
         "04-08-2015_14-52-37 0 0",
         "04-08-2015_14-59-30 0 0",
         "04-08-2015_14-59-30 0 0",
         "05-08-2015_15-46-30 0 0",
         "05-09-2015_15-46-38 0 0",
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
}
