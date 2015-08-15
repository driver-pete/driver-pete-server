package com.otognan.driverpete.logic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    @Autowired
    private TrajectoryEndpointRepository trajEndpointRepo;
    
    @Autowired
    private TrajectoryEndpointWorker worker;
    
    @Autowired
    AWSCredentials awsCredentials;
    
    public void updateTrajectoryEndpoint(TrajectoryEndpoint trajectoryEndpoint) {
        trajEndpointRepo.save(trajectoryEndpoint);
    }
    
    @Transactional(readOnly = true)
    public List<TrajectoryEndpoint> getUserTrajectoryEndpoint(User user) {
        System.out.println("Startin function...");
        worker.printThings("WORKER HELLO");
        String result =  "\"HELLO TRAJECTORY\"";
        System.out.println("Finishing function.");
        return trajEndpointRepo.findByUser(user);
    }

    public void processBinaryTrajectory(User user, String label, byte[] binaryTrajectory) {
        String bucketName = "driverpete-storage";
        String keyName = user.getUsername() + "/" + label;
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        try {
            System.out.println("Uploading a new object to S3 from a file\n");
            
            InputStream dataStream = new ByteArrayInputStream(binaryTrajectory);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(binaryTrajectory.length);
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
            throw ase;
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            throw ace;
        }
    }
    
}
