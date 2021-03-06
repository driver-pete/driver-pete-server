package com.otognan.driverpete.logic.routes;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.logic.ForbiddenDataException;
import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.logic.TrajectoryDownloadService;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RoutesService {
    
    @Autowired
    private RoutesStateRepository stateRepository;
    
    @Autowired
    private RoutesRepository routesRepository;
    
    @Autowired
    private TrajectoryDownloadService downloadService;
    
    private SecureRandom random = new SecureRandom();
    
    public void findRoutes(User user, List<Location> trajectory, List<Location> endpoints) throws Exception {
               
        // create filters
        RoutesFinder finder = new RoutesFinder(endpoints);
        
        RoutesState state = stateRepository.findOne(user.getId());
        
        System.out.println("Routes state: " + state);
        if (state != null) {
            finder.setFromEndpointIndex(state.getFromEndpointIndex());
            
            String currentRouteKey = state.getCurrentRouteKey();
            if (currentRouteKey != null) {
                List<Location> currentRoute = downloadService.downloadTrajectory(currentRouteKey);
                finder.setCurrentRoute(currentRoute);
            }

        }
         
        System.out.println("Processing data with routes finder..");
        for (Location point : trajectory) {
            finder.process(point);
        }
        
        System.out.println("Saving the state of the routes finder..");
        if (state == null) {
            state = new RoutesState();
            state.setUserId(user.getId());
        }
        state.setFromEndpointIndex(finder.getFromEndpointIndex());
        List<Location> currentRoute = finder.getCurrentRoute();
        if (currentRoute.size() > 0) {
            String keyToUpload = user.getUsername() + "/routes_state/current_route";
            downloadService.uploadTrajectory(keyToUpload, currentRoute);
            state.setCurrentRouteKey(keyToUpload);
        }
        
        stateRepository.save(state);
        
        System.out.println("Saving A to B routes..");
        this.saveRoutes(user, finder.getAtoBRoutes(), true);
        System.out.println("Saving B to A routes..");
        this.saveRoutes(user, finder.getBtoARoutes(), false);
    }
        
    public List<Route> getRoutes(User user, boolean isAtoB) {
        return this.routesRepository.findByUserAndDirectionAtoB(user, isAtoB);
    }

    public byte[] getBinaryRoute(User user, long routeId) throws IOException, ParseException {
        List<Route> existingRoutes = routesRepository.findByIdAndUser(routeId, user);
        if (existingRoutes.size() != 1) {
            throw new ForbiddenDataException("Can not find route with id for this user"); 
        }
        String routeKey = existingRoutes.get(0).getRouteKey();
        return this.downloadService.downloadBinaryTrajectory(routeKey);
    }
    
    private void saveRoutes(User user, List<List<Location>> routes, boolean isAtoB) throws Exception {
        List<Route> entities = new ArrayList<Route>();
        for (List<Location> trajectoryRoute : routes) {        
            String key = user.getUsername();
            if (isAtoB) {
                key += "/routes/a_to_b/";
            } else {
                key += "/routes/b_to_a/";
            }
            key += this.nextS3Id();
            
            System.out.println("Uploading routes " + key);
            downloadService.uploadTrajectory(key, trajectoryRoute);
            
            Route routeEntity = new Route();
            routeEntity.setUser(user);
            routeEntity.setDirectionAtoB(isAtoB);
            routeEntity.setRouteKey(key);
            routeEntity.setStartDate(trajectoryRoute.get(0).getTime());
            routeEntity.setFinishDate(trajectoryRoute.get(trajectoryRoute.size()-1).getTime());
            
            entities.add(routeEntity);
        }
                
        routesRepository.save(entities);
    }
    
    private String nextS3Id() {
        return new BigInteger(130, random).toString(32);
    }
    
    public void resetState(User user) {
        RoutesState state = stateRepository.findOne(user.getId());
        if (state != null) {
            String currentRouteKey = state.getCurrentRouteKey();
            if (currentRouteKey != null) {
                downloadService.deleteTrajectory(currentRouteKey);
            }
            stateRepository.delete(state);
        }
        // if database is dropped, we still want to delete the folder
        downloadService.deleteFolder(user.getUsername()+"/routes_state");
    }
    
    public void deleteAllRoutes(User user) {
        this.resetState(user);
        List<Route> routes = this.routesRepository.findByUser(user);
        for(Route route: routes) {
            downloadService.deleteTrajectory(route.getRouteKey());
        }
        this.routesRepository.delete(routes);
        // if database is dropped, we still want to delete the folder
        downloadService.deleteFolder(user.getUsername()+"/routes");
    }
}
