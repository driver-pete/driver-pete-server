package com.otognan.driverpete.logic;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={AWSConfiguration.class,
        TrajectoryReaderTest.TestPropertiesConfiguration.class})
public class TrajectoryReaderTest {
    
    @Autowired
    AWSCredentials awsCredentials;

    @Configuration
    static class TestPropertiesConfiguration {
        @Bean
        PropertyPlaceholderConfigurer propConfig() {
            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("security.properties"));
            ppc.setIgnoreResourceNotFound(true);
            return ppc;
        }
    }

    @Test
    public void testTrajectoryReader() throws IOException, ParseException {
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest("driverpete-storage", "_testing/testing_merged_0"));
        InputStream objectData = object.getObjectContent();
        
        List<Location> locations = TrajectoryReader.readTrajectory(objectData);
        
        //Process the objectData stream.
        objectData.close();
        
        assertEquals(2423, locations.size());
    }
    
    @Test
    public void testTrajectoryWriter() throws Exception {
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest("driverpete-storage", "_testing/testing_raw_0"));
        InputStream objectData = object.getObjectContent();
        
        List<Location> locations = TrajectoryReader.readTrajectory(objectData);

        //Process the objectData stream.
        objectData.close();
        
        byte[] compressedBytes = TrajectoryReader.writeTrajectory(locations);
        List<Location> locationsCopy = TrajectoryReader.readTrajectory(new ByteArrayInputStream(compressedBytes));

        Assert.assertThat(locationsCopy, 
                IsIterableContainingInOrder.contains(locations.toArray()));
    }
   
}
