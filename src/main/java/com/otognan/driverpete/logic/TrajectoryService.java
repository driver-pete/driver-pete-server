package com.otognan.driverpete.logic;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.logic.endpoints.TrajectoryEndpointsService;
import com.otognan.driverpete.logic.filtering.TrajectoryFilteringService;
import com.otognan.driverpete.logic.routes.RoutesService;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    
    @Autowired
    private TrajectoryDownloadService downloadService;
    
    @Autowired
    private TrajectoryFilteringService filteringService;
    
    @Autowired
    private TrajectoryEndpointsService endpointsService;
    
    @Autowired
    private RoutesService routesService;

    public void processBinaryTrajectory(User user, String label, byte[] binaryTrajectory) throws Exception {
        System.out.println("Starting to process trajectory " + label);
        String keyName = user.getUsername() + "/data/" + label;
        
        List<Location> trajectory = TrajectoryReader.readTrajectory(binaryTrajectory);
        if (trajectory.size() == 0) {
            System.out.println("Ignoring empty trajectory");
            return;
        }
        
        for (int i=0; i<trajectory.size()-1; i++) {
            if (trajectory.get(i+1).getTime() < trajectory.get(i).getTime()) {
                System.out.println("Ignoring inconsistent time trajectory");
                return; 
            }
        }
        
        // filter out data that for some reason breaks time continuity.
        // This happens when client sends the data, data is uploaded, but client for
        // some reason doesn't receive confirmation (networking error).
        // Client is going to resend this data again.
        Location latestUserLocation = this.getLatestLocation(user);
        if (latestUserLocation != null) {
            for (int i=0; i<trajectory.size(); i++) {
                if (trajectory.get(i).getTime() < latestUserLocation.getTime()) {
                    System.out.println("Ignoring duplicate time uploading");
                    return; 
                }
            }
        }
        
        downloadService.uploadBinaryTrajectory(keyName, binaryTrajectory);
        
        List<Location> endpoints = this.findEndpointsRoutine(user, keyName);
      
        if (endpoints.size() >= 2) {
           this.findRoutesInUnprocessedData(user, endpoints);
        }
    }
    
    public void resetProcessorsState(User user) {
        filteringService.resetState(user);
        endpointsService.resetState(user);
        routesService.resetState(user);
    }
    
    public void deleteAllEndpoints(User user) {
        endpointsService.deleteAllEndpoints(user);
        routesService.deleteAllRoutes(user);
    }
    
    public void deleteAllUserData(User user) {
        this.resetProcessorsState(user);
        this.deleteAllEndpoints(user);
        this.downloadService.deleteFolder(user.getUsername());
    }
    
    public void deleteProcessedUserData(User user) {
        this.resetProcessorsState(user);
        this.deleteAllEndpoints(user);
        this.deleteUnprocessedData(user);
    }
    
    private void findRoutesInUnprocessedData(User user, List<Location> endpoints) throws Exception {
        System.out.println("Going to find routes..");
        List<String> trajectoryKeys = downloadService.getTimedTrajectoryList(user.getUsername() + "/unprocessed");
        for (String key : trajectoryKeys) {
            System.out.println("Going to find routes in " + key);
            List<Location> trajectory = downloadService.downloadTrajectory(key);
            routesService.findRoutes(user, trajectory, endpoints);
            downloadService.deleteTrajectory(key);
        }
    }
    
    public void deleteUnprocessedData(User user) {
        downloadService.deleteFolder(user.getUsername() + "/unprocessed");
    }
    
    public void reprocessAllData(User user, boolean reprocessRoutes) throws Exception {
        this.deleteUnprocessedData(user);
        System.out.println("Reprocessing all data. Going to find endpoints first.");
        List<String> trajectoryKeys = downloadService.getTimedTrajectoryList(user.getUsername() + "/data");
        List<Location> endpoints = new ArrayList<Location>();
        for (String keyName : trajectoryKeys) {
            endpoints = this.findEndpointsRoutine(user, keyName);
        }
        
        if (reprocessRoutes && endpoints.size() >= 2) {
            this.findRoutesInUnprocessedData(user, endpoints);
        }
    }
    
    private List<Location> findEndpointsRoutine(User user, String keyName) throws Exception {
        System.out.println("Download trajectory " + keyName);
        byte[] binaryTrajectory = downloadService.downloadBinaryTrajectory(keyName);
        String label = keyName.substring(keyName.lastIndexOf("/")+1, keyName.length());
       
        List<Location> originalTrajectory = TrajectoryReader.readTrajectory(binaryTrajectory);
        System.out.println("Filtering trajectory of size " + originalTrajectory.size());
        List<Location> trajectory = filteringService.filterTrajectory(user, originalTrajectory);
        System.out.println("Filtered out " + (originalTrajectory.size() - trajectory.size()) + " point");
        
        System.out.println("Going to find enpoints..");
        List<Location> endpoints = endpointsService.findEndpoints(user, trajectory);
        
        System.out.println(endpoints.size() + " endpoints found.");
        
        String toProcessKey = user.getUsername() + "/unprocessed/" + label;
        downloadService.uploadTrajectory(toProcessKey, trajectory);
        
        return endpoints;
    }
    
    private Location getLatestLocation(User user) throws ParseException, IOException {
        List<String> existingTrajectoryKeys = downloadService.getTimedTrajectoryList(
                user.getUsername() + "/data");
        if (existingTrajectoryKeys.size() > 0) {
           String lastKey = existingTrajectoryKeys.get(existingTrajectoryKeys.size()-1);
           List<Location> latestTrajectory = downloadService.downloadTrajectory(lastKey);
           return latestTrajectory.get(latestTrajectory.size() - 1);
        } else {
            return null;
        }
    }
}
