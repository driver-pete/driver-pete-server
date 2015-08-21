package com.otognan.driverpete.logic;

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
        String keyName = user.getUsername() + "/" + label;
        downloadService.uploadBinaryTrajectory(keyName, binaryTrajectory);

        String toProcessKey = user.getUsername() + "/unprocessed/" + label;
        downloadService.copyTrajectory(keyName, toProcessKey);
        
        //download trajectory
        System.out.println("Download trajectory copy..");
        List<Location> originalTrajectory = downloadService.downloadTrajectory(toProcessKey);
        System.out.println("Filtering trajectory of size " + originalTrajectory.size());
        List<Location> trajectory = filteringService.filterTrajectory(user, originalTrajectory);
        System.out.println("Filtered out " + (originalTrajectory.size() - trajectory.size()) + " point");
        
        System.out.println("Going to find enpoints..");
        List<Location> endpoints = endpointsService.findEndpoints(user, trajectory);
        
        System.out.println(endpoints.size() + " endpoints found.");
        if (endpoints.size() >= 2) {
            System.out.println("Going to find routes..");
            routesService.findRoutes(user, trajectory, endpoints);
        }
        
        downloadService.deleteTrajectory(toProcessKey);
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
    
}
