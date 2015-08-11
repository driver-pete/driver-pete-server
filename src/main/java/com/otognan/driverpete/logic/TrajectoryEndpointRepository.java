package com.otognan.driverpete.logic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otognan.driverpete.security.User;



public interface TrajectoryEndpointRepository extends JpaRepository<TrajectoryEndpoint, Long> {
    List<TrajectoryEndpoint> findByUser(User user);
}


