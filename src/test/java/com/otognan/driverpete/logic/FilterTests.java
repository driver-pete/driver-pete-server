package com.otognan.driverpete.logic;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class FilterTests extends BaseStatelesSecurityITTest{
    
    @Autowired
    private AWSCredentials awsCredentials;
    
    private List<Location> testData;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
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
    
    @Test
    public void testRemoveDuplicateTimeReading() {
        TrajectoryFilter filter = new DuplicateTimeFilter();
        List<Location> filtered = ApplyTrajectoryFilter.apply(this.testData, filter);
        
        int expectedNumberOfDuplicates = 2;
        assertEquals(this.testData.size() - expectedNumberOfDuplicates,
                filtered.size());
    }
}
