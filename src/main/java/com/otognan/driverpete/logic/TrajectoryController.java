package com.otognan.driverpete.logic;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryController {
    
    @Autowired
    AWSCredentials awsCredentials;

    // Method for testing of binary upload
    @RequestMapping(value = "/api/trajectory/compressed_length",
            method = RequestMethod.POST)
    public int compressedLength(HttpEntity<byte[]> requestEntity) throws Exception {  
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
        return payload.length;
    }
    
    @RequestMapping(value = "/api/trajectory/compressed",
            method = RequestMethod.POST)
    public void uploadTrajectory(Principal principal,
            @RequestParam("label") String label,
            HttpEntity<byte[]> requestEntity) throws Exception {  
        
        User user = (User)((Authentication)principal).getPrincipal(); 
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
         
        String bucketName = "driverpete-storage";
        String keyName = user.getUsername() + "/" + label;
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        try {
            System.out.println("Uploading a new object to S3 from a file\n");
            
            InputStream dataStream = new ByteArrayInputStream(payload);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(payload.length);
            s3client.putObject(new PutObjectRequest(bucketName, keyName, dataStream, meta));

         } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
    }

}