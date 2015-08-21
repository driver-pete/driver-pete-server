package com.otognan.driverpete.logic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TrajectoryReader {
    
    public static List<Location> readTrajectory(InputStream input) throws IOException, ParseException {
        GZIPInputStream gis = new GZIPInputStream(input);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        
        ArrayList<Location> locations = new ArrayList<Location>();
        
        String line;
        while ((line=bf.readLine())!=null) {
            locations.add(Location.fromString(line));
        }
        return locations;
    }
    
    public static List<Location> readTrajectory(byte[] trajectoryBytes) throws IOException, ParseException {
        return TrajectoryReader.readTrajectory(new ByteArrayInputStream(trajectoryBytes));
    }
    
    public static byte[] writeTrajectory(List<Location> locations) throws Exception {
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        
        String stringData = "";
        for (Location l : locations)
        {
            stringData += l.toSerializationString() + "\n";
        }

        gzip.write(stringData.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
    }

}
