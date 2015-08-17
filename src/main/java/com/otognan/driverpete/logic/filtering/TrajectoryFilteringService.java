package com.otognan.driverpete.logic.filtering;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryFilteringService {
    
    @Autowired
    FilteringStateRepository stateRepository;
    
    public List<Location> filterTrajectory(User user, List<Location> trajectory) {
               
        // create filters
        DuplicateTimeFilter duplicateTime = new DuplicateTimeFilter();
        StationaryPointsFilter stationaryPoint = new StationaryPointsFilter();
        VelocityOutliersFilter velocityOutlier = new VelocityOutliersFilter(85.);
        
        // get user state for endpoints and put state into filters
        FilteringState state = stateRepository.findOne(user.getId());
        if (state != null) {
            velocityOutlier.setOutliersCounter(state.getVelocityOutliersCounter());
        }
        
        // extract endpoints
        TrajectoryFilter chain[] = {duplicateTime,
                stationaryPoint,
                velocityOutlier};
        
        List<Location> filtered = TrajectoryFilterUtils.apply(trajectory,
                new TrajectoryFilterChain(chain));

        if (state == null) {
            state = new FilteringState();
            state.setUserId(user.getId());
        }
        state.setVelocityOutliersCounter(velocityOutlier.getOutliersCounter());
        
        stateRepository.save(state);
        
        return filtered;
    }
    
}
