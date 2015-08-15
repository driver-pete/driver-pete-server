package com.otognan.driverpete.logic;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
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


public class FindRoutesTest extends BaseStatelesSecurityITTest{
    
    @Autowired
    private AWSCredentials awsCredentials;
    
    private List<Location> testData;
        
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest("driverpete-storage", "_testing/testing_merged_1"));
        InputStream objectData = object.getObjectContent();
        
        List<Location> data = TrajectoryReader.readTrajectory(objectData);
        objectData.close();
        this.testData = TrajectoryFilterUtils.filterGPSData(data);
    }
    
    @Test
    public void testFindEndpoints() {
        List<Location> endpoints = FindEndpointsProcessor.findEndpoints(this.testData);
        
        assertEquals(2, endpoints.size());
        assertThat(this.testData.get(478), equalTo(endpoints.get(0)));
        assertThat(this.testData.get(669), equalTo(endpoints.get(1)));
    }    
}
