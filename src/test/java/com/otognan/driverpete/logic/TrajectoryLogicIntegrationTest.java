package com.otognan.driverpete.logic;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {
    
    @Autowired
    AWSCredentials awsCredentials;
    
    private TrajectoryLogicApi server() throws Exception {
        String token = this.getTestToken();
        return this.serverAPI(token, TrajectoryLogicApi.class);
    }

    @Test
    public void determineDataLength() throws Exception {
        String inputStr = "   &*()!@#$$%^&())((     ()/n/ndfgsd)(*)(@''''???";
        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes("UTF-8"));
        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
        
        int dataLenth = this.server().compressedLength(in);
        
        assertThat(dataLenth, equalTo(inputStr.length()));
    }
    
    @Test
    public void uploadToS3SucceedsEvenIfBadData() throws Exception {
        String inputStr = "Hello. I'm not a trajectory";
        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes());
        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
        
        String trajectoryName = "_my_test_bad_trajectory";
        this.server().compressed(trajectoryName, in);
        
        // Check that file is there
        AmazonS3 s3Client = new AmazonS3Client(awsCredentials);  
        
        String uploadedKey = "TestMike/" + trajectoryName;
        
        S3Object object = s3Client.getObject(new GetObjectRequest("driverpete-storage", uploadedKey));
        
        String outputStr = IOUtils.toString(new InputStreamReader(
                object.getObjectContent()));
        
        s3Client.deleteObject("driverpete-storage", uploadedKey);
        
        assertThat(inputStr, equalTo(outputStr));
    }
    
    
    @Test
    public void findEndpoints() throws Exception {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest("driverpete-storage", "_testing/testing_merged_1"));
        InputStream objectData = object.getObjectContent();  
        byte[] trajectoryBytes = IOUtils.toByteArray(objectData);
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);
        objectData.close();
         
        String trajectoryName = "_my_trajectory";
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));
        
        List<TrajectoryEndpoint> endpoints = this.server().trajectoryEndpoints();
        
        assertThat(endpoints.size(), equalTo(2));
        
        List<Location> data = TrajectoryReader.readTrajectory(new ByteArrayInputStream(trajectoryBytes));
        data = TrajectoryFilterUtils.filterGPSData(data);
                
        assertThat(data.get(478).getLatitude(), equalTo(endpoints.get(0).getLatitude()));
        assertThat(data.get(478).getLongitude(), equalTo(endpoints.get(0).getLongitude()));
       
        assertThat(data.get(669).getLatitude(), equalTo(endpoints.get(1).getLatitude()));
        assertThat(data.get(669).getLongitude(), equalTo(endpoints.get(1).getLongitude()));
    }
}
