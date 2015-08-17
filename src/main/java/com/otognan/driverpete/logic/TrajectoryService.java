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
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    @Autowired
    private TrajectoryEndpointRepository trajEndpointRepo;
    
    @Autowired
    TrajectoryEndpointStateRepository stateRepository;
    
    @Autowired
    AWSCredentials awsCredentials;
    
    public void updateTrajectoryEndpoint(TrajectoryEndpoint trajectoryEndpoint) {
        trajEndpointRepo.save(trajectoryEndpoint);
    }
    
    @Transactional(readOnly = true)
    public List<TrajectoryEndpoint> getUserTrajectoryEndpoint(User user) {
//        System.out.println("Startin function...");
//        worker.printThings("WORKER HELLO");
//        String result =  "\"HELLO TRAJECTORY\"";
//        System.out.println("Finishing function.");
        return trajEndpointRepo.findByUser(user);
    }

    //@Transactional
    public void processBinaryTrajectory(User user, String label, byte[] binaryTrajectory) {
        String bucketName = "driverpete-storage";
        String keyName = user.getUsername() + "/" + label;
        
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        try {
            System.out.println("Uploading a new object to S3 from a array of size " + binaryTrajectory.length);
            
            InputStream dataStream = new ByteArrayInputStream(binaryTrajectory);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(binaryTrajectory.length);
            s3client.putObject(new PutObjectRequest(bucketName, keyName, dataStream, meta));

            String toProcessKey = user.getUsername() + "/unprocessed/" + label;
            s3client.copyObject(bucketName, keyName, bucketName, toProcessKey);
            
            try {
                this.findEndpoints(user, toProcessKey);
            } catch (IOException | ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
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
    
    //@Async
    public void findEndpoints(User user, String trajectoryKey) throws IOException, ParseException {
        
        List<TrajectoryEndpoint> trajectoryEndpointsEntities = trajEndpointRepo.findByUser(user);
        if (trajectoryEndpointsEntities.size() >= 2) {
            System.out.println("More than 2 endpoints is not supported");
            return;
        }
        
        //download trajectory
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest("driverpete-storage", trajectoryKey));
        InputStream objectData = object.getObjectContent();
        List<Location> trajectory = TrajectoryReader.readTrajectory(objectData);
        objectData.close();
        
        // create filters
        DuplicateTimeFilter duplicateTime = new DuplicateTimeFilter();
        StationaryPointsFilter stationaryPoint = new StationaryPointsFilter();
        VelocityOutliersFilter velocityOutlier = new VelocityOutliersFilter(85.);

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
            velocityOutlier.setOutliersCounter(state.getVelocityOutliersCounter());
            processor.setPreviousPoint(state.getProcessorPreviousPoint());
        }
        
        // extract endpoints
        TrajectoryFilter chain[] = {duplicateTime,
                stationaryPoint,
                velocityOutlier};
        
        List<Location> filtered = TrajectoryFilterUtils.apply(trajectory,
                new TrajectoryFilterChain(chain));
        // get endpoitns
        for (Location location : filtered) {
            processor.process(location);
        }
        

        for (int i = originalEndpointsSize; i < Math.min(endpoints.size(), 2); i++) {
            TrajectoryEndpoint endpointEntity = new TrajectoryEndpoint();
            endpointEntity.setUser(user);
            endpointEntity.setLatitude(endpoints.get(i).getLatitude());
            endpointEntity.setLongitude(endpoints.get(i).getLongitude());
            
            System.out.println("SAVING ENDPOINT!!!!");
            trajEndpointRepo.save(endpointEntity);
        }

        if (state == null) {
            state = new EndpointProcessorState();
            state.setUserId(user.getId());
        }
        state.setVelocityOutliersCounter(velocityOutlier.getOutliersCounter());
        state.setProcessorPreviousPoint(processor.getPreviousPoint());
        
        stateRepository.save(state);
        
        //s3client.deleteObject("driverpete-storage", trajectoryKey);
    }
    
}
