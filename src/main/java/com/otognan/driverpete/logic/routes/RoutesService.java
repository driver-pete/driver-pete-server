package com.otognan.driverpete.logic.routes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.logic.TrajectoryDownloadService;
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
    
    public void findRoutes(User user, List<Location> trajectory, List<Location> endpoints) throws Exception {
               
        // create filters
        RoutesFinder finder = new RoutesFinder(endpoints);
        
        RoutesState state = stateRepository.findOne(user.getId());
        if (state != null) {
            
            finder.setFromEndpointIndex(state.getFromEndpointIndex());
            
            String currentRouteKey = state.getCurrentRouteKey();
            if (currentRouteKey != null) {
                List<Location> currentRoute = downloadService.downloadTrajectory(currentRouteKey);
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
            downloadService.uploadTrajectory(keyToUpload, currentRoute);
        }
        
        stateRepository.save(state);
        
        this.saveRoutes(user, finder.getAtoBRoutes(), true);
        this.saveRoutes(user, finder.getBtoARoutes(), false);
    }

    
    private void saveRoutes(User user, List<List<Location>> routes, boolean isAtoB) throws Exception {
        for (List<Location> trajectoryRoute : routes) {        
            String key = user.getUsername();
            if (isAtoB) {
                key += "/routes/a_to_b";
            } else {
                key += "/routes/b_to_a";
            }
            downloadService.uploadTrajectory(key, trajectoryRoute);
            
            Route routeEntity = new Route();
            routeEntity.setUser(user);
            routeEntity.setDirectionAtoB(isAtoB);
            routeEntity.setRouteKey(key);
            
            routesRepository.save(routeEntity);
        }
    }
}
