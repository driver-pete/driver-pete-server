package com.otognan.driverpete.logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;
import com.otognan.driverpete.logic.filtering.DuplicateTimeFilter;
import com.otognan.driverpete.logic.filtering.TrajectoryFilter;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterUtils;
import com.otognan.driverpete.logic.filtering.VelocityOutliersFilter;


public class FilterTests extends BaseStatelesSecurityITTest{
    
    @Autowired
    private AWSCredentials awsCredentials;
    
    private List<Location> testData;
        
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        if (this.testData == null) {
            AmazonS3 s3client = new AmazonS3Client(awsCredentials);
            S3Object object = s3client.getObject(
                    new GetObjectRequest("driverpete-storage", "_testing/testing_raw_0"));
            InputStream objectData = object.getObjectContent();
            
            List<Location> data = TrajectoryReader.readTrajectory(objectData);
            objectData.close();
    
            //add few time duplicates
            data.set(54, data.get(53));
            data.set(100, data.get(99));
                
            // add few distance duplicates
            data.set(23, new Location(data.get(23).getTime(),
                data.get(22).getLatitude(), data.get(22).getLongitude()));
            data.set(40, new Location(data.get(40).getTime(),
                    data.get(39).getLatitude(), data.get(39).getLongitude()));
            data.set(60, new Location(data.get(60).getTime(),
                    data.get(59).getLatitude(), data.get(59).getLongitude()));
            this.testData = data;
        }
    }
    
    @Test
    public void testRemoveDuplicateTimeReading() {
        TrajectoryFilter filter = new DuplicateTimeFilter();
        List<Location> filtered = TrajectoryFilterUtils.apply(this.testData, filter);
        
        int expectedNumberOfDuplicates = 2;
        assertEquals(this.testData.size() - expectedNumberOfDuplicates,
                filtered.size());
    }
    
    @Test
    public void testRemoveOutliers() {
        List<Location> data = TrajectoryFilterUtils.apply(this.testData, new DuplicateTimeFilter());
          
        double velocityThreshold = 85.;
     
        // check that data has outliers in velocity and distance
        List<Integer> outliers = this.velocityOutliers(data, velocityThreshold);
        assertThat(outliers.size(),  greaterThan(0));
        assertThat(this.maxDist(data), greaterThan(157900.));
        
        TrajectoryFilter filter = new VelocityOutliersFilter(velocityThreshold);   
        List<Location> fixedData = TrajectoryFilterUtils.apply(data, filter);

        // no large velocities left
        assertEquals(this.velocityOutliers(fixedData, velocityThreshold).size(), 0);
        assertThat(this.maxDist(fixedData), lessThan(330.));
        
        // we expect 5 point to be removed
        assertEquals(data.size() - fixedData.size(),  5);
    }
    
    @Test
    public void testRemoveStationaryNoise() {
        /*
        The data has large amount of noise - switching between SD and LA every 10 seconds.
        It starts from SD, then noise, later it returns to SD. We test that LA is ignored
        */
        List<Location> data = TrajectoryFilterUtils.apply(this.testData, new DuplicateTimeFilter());
        data = data.subList(561, 576);

        List<Location> fixedData = TrajectoryFilterUtils.apply(data, new VelocityOutliersFilter(85.));

        Location stationaryPoint = new Location(0, 33.004964, -117.060207);
        
        assertEquals(11,  fixedData.size());
        for (Location location : fixedData) {
            assertThat(Location.distance(stationaryPoint, location), lessThan(246.6));
        }
    }
    
    @Test
    public void testRemoveStationaryNoiseReturnToStable() {
        /*
        The data has large amount of noise - switching between SD and LA every 10 seconds.
        It starts from the noisy point, later it returns to SD.
        Here we test that even if data starts with noisy value, we still converge
        to stable point
         */
        List<Location> data = TrajectoryFilterUtils.apply(this.testData, new DuplicateTimeFilter());
        data = data.subList(563, 576);

        List<Location> fixedData = TrajectoryFilterUtils.apply(data, new VelocityOutliersFilter(85.));

        Location stationaryPoint = new Location(0, 33.004964, -117.060207);
        
        assertEquals(7,  fixedData.size());
        for (int i=0; i < 4; i++) {
            assertThat(Location.distance(stationaryPoint, fixedData.get(i)), greaterThan(157000.));
        }
        
        for (int i=4; i < fixedData.size(); i++) {
            assertThat(Location.distance(stationaryPoint, fixedData.get(i)), lessThan(246.6));
        }
    }
    
    @Test
    public void testFilterGPS() {
        List<Location> filtered = TrajectoryFilterUtils.filterGPSData(this.testData);
        assertEquals(11, this.testData.size() - filtered.size());
    }
    
    private List<Integer> velocityOutliers(List<Location> data, double velocityMph) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<data.size()-1; i++) {
            double v = Location.velocityMph(data.get(i), data.get(i+1));
            if (v > velocityMph) {
                result.add(i);
            }
        }
        return result;
    }
 
    private double maxDist(List<Location> data) {
        double maxDist = 0;
        for (int i=0; i<data.size()-1; i++) {
            double ds = Location.distance(data.get(i), data.get(i+1));
            if (ds > maxDist) {
                maxDist = ds;
            }
        }
        return maxDist;
    }
}
