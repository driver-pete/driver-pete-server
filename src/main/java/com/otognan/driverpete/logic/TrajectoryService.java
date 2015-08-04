package com.otognan.driverpete.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.otognan.driverpete.security.User;


@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrajectoryService {
    @Autowired
    private TrajectoryEndpointRepository trajEndpointRepo;
    
    public void updateTrajectoryEndpoint(TrajectoryEndpoint trajectoryEndpoint) {
        trajEndpointRepo.save(trajectoryEndpoint);
    }
    
    @Transactional(readOnly = true)
    public List<TrajectoryEndpoint> getUserTrajectoryEndpoint(User user) {
        return trajEndpointRepo.findByUser(user);
    }

}
