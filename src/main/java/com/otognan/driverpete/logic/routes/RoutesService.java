package com.otognan.driverpete.logic.routes;

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
import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.logic.TrajectoryReader;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RoutesService {
    
    @Autowired
    RoutesStateRepository stateRepository;
    
    static private final String BUCKET_NAME = "driverpete-storage";
    
    @Autowired
    AWSCredentials awsCredentials;
    
    public void findRoutes(User user, List<Location> trajectory, List<Location> endpoints) throws Exception {
               
        // create filters
        RoutesFinder finder = new RoutesFinder(endpoints);
        
        RoutesState state = stateRepository.findOne(user.getId());
        if (state != null) {
            
            finder.setFromEndpointIndex(state.getFromEndpointIndex());
            
            String currentRouteKey = state.getCurrentRouteKey();
            if (currentRouteKey != null) {
                List<Location> currentRoute = this.downloadTrajectory(currentRouteKey);
                finder.setCurrentRoute(currentRoute);
            }
        }
        
        for (Location point : trajectory) {
            finder.process(point);
        }
        
        if (state == null) {
            state = new RoutesState();
            state.setUserId(user.getId());
        }
        state.setFromEndpointIndex(finder.getFromEndpointIndex());
        List<Location> currentRoute = finder.getCurrentRoute();
        if (currentRoute.size() > 0) {
            String keyToUpload = user.getUsername() + "/routes_state/current_route";
            this.uploadTrajectory(keyToUpload, currentRoute);
        }
        
        stateRepository.save(state);

    }
    
    private List<Location> downloadTrajectory(String key) throws IOException, ParseException {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest(BUCKET_NAME, key));
        InputStream objectData = object.getObjectContent();
        List<Location> trajectory = TrajectoryReader.readTrajectory(objectData);
        objectData.close();
        return trajectory;
    }
    
    private void uploadTrajectory(String key, List<Location> trajectory) throws Exception {
        byte[] binaryTrajectory = TrajectoryReader.writeTrajectory(trajectory);
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        InputStream dataStream = new ByteArrayInputStream(binaryTrajectory);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(binaryTrajectory.length);
        s3client.putObject(new PutObjectRequest(BUCKET_NAME, key, dataStream, meta));
    }
    
}
