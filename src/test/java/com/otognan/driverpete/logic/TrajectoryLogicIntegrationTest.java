package com.otognan.driverpete.logic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.InputStreamReader;

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

//    @Test
//    public void determineDataLength() throws Exception {
//        String inputStr = "   &*()!@#$$%^&())((     ()/n/ndfgsd)(*)(@''''???";
//        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes("UTF-8"));
//        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
//        
//        int dataLenth = this.server().compressedLength(in);
//        
//        assertThat(dataLenth, equalTo(inputStr.length()));
//    }
    
    @Test
    public void uploadToS3() throws Exception {
        String inputStr = "Hello. I'm going to be uploaded to S3";
        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes());
        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
        
        String trajectoryName = "_my_test_trajectory";
        this.server().compressed(trajectoryName, in);
        
        // Check that file is there
        AmazonS3 s3Client = new AmazonS3Client(awsCredentials);      
        S3Object object = s3Client.getObject(new GetObjectRequest("driverpete-storage", trajectoryName));
        
        String outputStr = IOUtils.toString(new InputStreamReader(
                object.getObjectContent()));
        
        s3Client.deleteObject("driverpete-storage", trajectoryName);
        
        assertThat(inputStr, equalTo(outputStr));
    }
    
}
