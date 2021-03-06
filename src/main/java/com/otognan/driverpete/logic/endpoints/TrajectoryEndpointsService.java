package com.otognan.driverpete.logic.endpoints;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.logic.ForbiddenDataException;
import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryEndpointsService {

    @Autowired
    private TrajectoryEndpointRepository trajEndpointRepo;
    
    @Autowired
    private TrajectoryEndpointStateRepository stateRepository;
    
    public void updateTrajectoryEndpoint(TrajectoryEndpoint trajectoryEndpoint) {
        trajEndpointRepo.save(trajectoryEndpoint);
    }
    
    @Transactional(readOnly = true)
    public List<TrajectoryEndpoint> getUserTrajectoryEndpoint(User user) {
        return trajEndpointRepo.findByUser(user);
    }
    
    public List<Location> findEndpoints(User user, List<Location> filteredTrajectory) throws Exception  {
        List<TrajectoryEndpoint> trajectoryEndpointsEntities = trajEndpointRepo.findByUser(user);
        
        List<Location> endpoints = endpointsEntitiesToLocation(trajectoryEndpointsEntities);
        if (endpoints.size() == 2) {
            System.out.println("Found 2 endpoints in the database.");
            return endpoints;
        }
        
        if (endpoints.size() > 2) {
            throw new Exception("More than 2 endpoints is not supported");
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
        
        if (endpoints.size() > 2) {
            throw new Exception("Endpoint processor returned more than 2 endpoints");
        }
        
        for (int i = originalEndpointsSize; i < Math.min(endpoints.size(), 2); i++) {
            TrajectoryEndpoint endpointEntity = new TrajectoryEndpoint();
            endpointEntity.setUser(user);
            Location location = endpoints.get(i);
            endpointEntity.setLatitude(location.getLatitude());
            endpointEntity.setLongitude(location.getLongitude());
            if (i == 0) endpointEntity.setLabel("A");
            if (i == 1) endpointEntity.setLabel("B"); 
            endpointEntity.setAddress(location.getAddress());

            trajEndpointRepo.save(endpointEntity);
        }

        if (state == null) {
            state = new EndpointProcessorState();
            state.setUserId(user.getId());
        }
        state.setProcessorPreviousPoint(processor.getPreviousPoint());
        stateRepository.save(state);
        
        return endpoints;
    }
    
    private List<Location> endpointsEntitiesToLocation(List<TrajectoryEndpoint> entities) {
        List<Location> locations = new ArrayList<Location>();
        for (TrajectoryEndpoint endpoint : entities) {
            Location location = new Location(System.currentTimeMillis(), endpoint.getLatitude(),
                    endpoint.getLongitude());
            locations.add(location);
        }
        return locations;
    }
    
    public void resetState(User user) {
        EndpointProcessorState state = stateRepository.findOne(user.getId());
        if (state != null) {
            stateRepository.delete(state);
        }
    }
    
    public void deleteAllEndpoints(User user) {
        this.resetState(user);
        List<TrajectoryEndpoint> trajectoryEndpointsEntities = trajEndpointRepo.findByUser(user);
        trajEndpointRepo.delete(trajectoryEndpointsEntities);
    }
    
    public void editEndpoint(User user, TrajectoryEndpoint endpoint) throws Exception {
        List<TrajectoryEndpoint> existingEndpoints = trajEndpointRepo.findByIdAndUser(endpoint.getId(), user);
        if (existingEndpoints.size() != 1) {
            throw new ForbiddenDataException("Can not find trajectory with id for this user"); 
        }
        endpoint.setUser(user);
        trajEndpointRepo.save(endpoint);
    }
}
