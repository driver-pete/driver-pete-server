package com.otognan.driverpete.logic;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

public class LocationTest {

    @Test
    public void testLocationFromString() throws ParseException {
        String locationString = "04-08-2015_14-35-50 32.936004 -117.235370";
        Location l = Location.fromString(locationString);
        
        //Date date = new Date(l.getTime());
        assertEquals(1438724150000l, l.getTime());
        assertEquals(32.936004, l.getLatitude(), 1e-10);
        assertEquals(-117.235370, l.getLongitude(), 1e-10);
        
        assertEquals(locationString, l.toSerializationString());
    }

}
