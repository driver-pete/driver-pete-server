package com.otognan.driverpete.logic;


import java.security.Principal;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryController {
    
    @Autowired
    private TrajectoryService trajectoryService;
    
    @RequestMapping(value = "/api/trajectory/compressed",
            method = RequestMethod.POST)
    public void uploadTrajectory(Principal principal,
            @RequestParam("label") String label,
            HttpEntity<byte[]> requestEntity) throws Exception {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
        trajectoryService.processBinaryTrajectory(user, label, payload);
    }

    @RequestMapping(value = "/api/trajectory/endpoints/all", method = RequestMethod.DELETE)
    public void deleteAllEndpoints(Principal principal) {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        trajectoryService.deleteAllEndpoints(user);
    }
    
    @RequestMapping(value = "/api/trajectory/state", method = RequestMethod.DELETE)
    public void resetProcessorState(Principal principal) {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        trajectoryService.resetProcessorsState(user);
    }
    
    @RequestMapping(value = "/api/trajectory/all", method = RequestMethod.DELETE)
    public void deleteAllUserData(Principal principal) {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        trajectoryService.deleteAllUserData(user);
    }
    
    @RequestMapping(value = "/api/trajectory/reprocess/all", method = RequestMethod.POST)
    public void reprocessAllUserData(Principal principal) throws Exception {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        trajectoryService.reprocessAllData(user);
    }
}