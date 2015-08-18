package com.otognan.driverpete.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpointsService;
import com.otognan.driverpete.logic.filtering.TrajectoryFilteringService;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    static private final String BUCKET_NAME = "driverpete-storage";
    
    @Autowired
    private TrajectoryFilteringService filteringService;
    
    @Autowired
    private TrajectoryEndpointsService endpointsService;
    
    @Autowired
    private TrajectoryDownloadService downloadService;
    
    @Autowired
    private AWSCredentials awsCredentials;

    //@Transactional
    public void processBinaryTrajectory(User user, String label, byte[] binaryTrajectory) throws Exception {
        String keyName = user.getUsername() + "/" + label;
        downloadService.uploadBinaryTrajectory(keyName, binaryTrajectory);

        String toProcessKey = user.getUsername() + "/unprocessed/" + label;
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        s3client.copyObject(BUCKET_NAME, keyName, BUCKET_NAME, toProcessKey);
        
        //download trajectory
        List<Location> originalTrajectory = downloadService.downloadTrajectory(toProcessKey);
        List<Location> trajectory = filteringService.filterTrajectory(user, originalTrajectory);
        
        endpointsService.findEndpoints(user, trajectory);
        
        s3client.deleteObject(BUCKET_NAME, toProcessKey);
    }    
}
