package com.otognan.driverpete.logic;


import java.security.Principal;

import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryController {

    @RequestMapping(value = "/api/trajectory/compressed", method = RequestMethod.GET)
    public String getCurrent(Principal principal,
            HttpEntity<byte[]> requestEntity) {  
        User user = (User)((Authentication) principal).getPrincipal();
        byte[] payload = requestEntity.getBody();

        return Integer.toString(payload.length);
    }
    
    @RequestMapping(value = "/api/trajectory/hello", method = RequestMethod.GET)
    public String getCurrent(Principal principal) {  

        return "HELLO";
    }
}