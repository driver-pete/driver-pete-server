package com.otognan.driverpete.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.logic.endpoints.EndpointProcessorState;
import com.otognan.driverpete.logic.endpoints.FindEndpointsProcessor;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpointRepository;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpointStateRepository;
import com.otognan.driverpete.logic.filtering.DuplicateTimeFilter;
import com.otognan.driverpete.logic.filtering.StationaryPointsFilter;
import com.otognan.driverpete.logic.filtering.TrajectoryFilter;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterChain;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterUtils;
import com.otognan.driverpete.logic.filtering.TrajectoryFilteringService;
import com.otognan.driverpete.logic.filtering.VelocityOutliersFilter;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    static private final String BUCKET_NAME = "driverpete-storage";
    
    @Autowired
    private TrajectoryEndpointRepository trajEndpointRepo;
    
    @Autowired
    TrajectoryEndpointStateRepository stateRepository;
    
    @Autowired
    TrajectoryFilteringService filteringService;
    
    @Autowired
    AWSCredentials awsCredentials;
    
    public void updateTrajectoryEndpoint(TrajectoryEndpoint trajectoryEndpoint) {
        trajEndpointRepo.save(trajectoryEndpoint);
    }
    
    @Transactional(readOnly = true)
    public List<TrajectoryEndpoint> getUserTrajectoryEndpoint(User user) {
        return trajEndpointRepo.findByUser(user);
    }

    //@Transactional
    public void processBinaryTrajectory(User user, String label, byte[] binaryTrajectory) throws IOException, ParseException {
        String keyName = user.getUsername() + "/" + label;
        this.uploadTrajectory(keyName, binaryTrajectory);

        String toProcessKey = user.getUsername() + "/unprocessed/" + label;
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        s3client.copyObject(BUCKET_NAME, keyName, BUCKET_NAME, toProcessKey);
        
        //download trajectory
        List<Location> originalTrajectory = this.downloadTrajectory(toProcessKey);
        List<Location> trajectory = filteringService.filterTrajectory(user, originalTrajectory);
        
        this.findEndpoints(user, trajectory);
        
        s3client.deleteObject(BUCKET_NAME, toProcessKey);
    }
    
    public void findEndpoints(User user, List<Location> filteredTrajectory) throws IOException, ParseException {
        
        List<TrajectoryEndpoint> trajectoryEndpointsEntities = trajEndpointRepo.findByUser(user);
        if (trajectoryEndpointsEntities.size() >= 2) {
            System.out.println("More than 2 endpoints is not supported");
            return;
        }
        
        List<Location> endpoints = new ArrayList<Location>();
        for (TrajectoryEndpoint endpoint : trajectoryEndpointsEntities) {
            Location location = new Location(System.currentTimeMillis(), endpoint.getLatitude(),
                    endpoint.getLongitude());
            endpoints.add(location);
        }
        
        int originalEndpointsSize = endpoints.size();
        FindEndpointsProcessor processor = new FindEndpointsProcessor(endpoints);
        
        // get user state for endpoints and put state into filters
        EndpointProcessorState state = stateRepository.findOne(user.getId());
        if (state != null) {
            processor.setPreviousPoint(state.getProcessorPreviousPoint());
        }
                
        // get endpoitns
        for (Location location : filteredTrajectory) {
            processor.process(location);
        }

        for (int i = originalEndpointsSize; i < Math.min(endpoints.size(), 2); i++) {
            TrajectoryEndpoint endpointEntity = new TrajectoryEndpoint();
            endpointEntity.setUser(user);
            endpointEntity.setLatitude(endpoints.get(i).getLatitude());
            endpointEntity.setLongitude(endpoints.get(i).getLongitude());

            trajEndpointRepo.save(endpointEntity);
        }

        if (state == null) {
            state = new EndpointProcessorState();
            state.setUserId(user.getId());
        }
        state.setProcessorPreviousPoint(processor.getPreviousPoint());
        
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
    
    private void uploadTrajectory(String key, byte[] binaryTrajectory) {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        InputStream dataStream = new ByteArrayInputStream(binaryTrajectory);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(binaryTrajectory.length);
        s3client.putObject(new PutObjectRequest(BUCKET_NAME, key, dataStream, meta));
    }
}
