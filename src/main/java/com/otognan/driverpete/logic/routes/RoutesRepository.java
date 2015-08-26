package com.otognan.driverpete.logic.routes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.security.User;

public interface RoutesRepository extends JpaRepository<Route, Long> {
    
    List<Route> findByUser(User user);
    List<Route> findByIdAndUser(Long id, User user);
    List<Route> findByUserAndDirectionAtoB(User user, boolean directionAtoB); 

}
