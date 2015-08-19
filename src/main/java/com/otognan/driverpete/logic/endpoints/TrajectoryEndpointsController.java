package com.otognan.driverpete.logic.endpoints;


import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryEndpointsController {
    
    @Autowired
    private TrajectoryEndpointsService trajectoryEndpointsService;
    
    @RequestMapping(value = "/api/trajectory/endpoints", method = RequestMethod.GET)
    public List<TrajectoryEndpoint> trajectoryEndpoints(Principal principal) {
        User user = (User)((Authentication)principal).getPrincipal();
        return this.trajectoryEndpointsService.getUserTrajectoryEndpoint(user);
    }

}