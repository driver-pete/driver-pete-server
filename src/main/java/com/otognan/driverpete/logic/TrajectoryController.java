package com.otognan.driverpete.logic;


import java.security.Principal;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryController {

    @RequestMapping(value = "/api/trajectory/compressed",
            method = RequestMethod.POST)
    public int getCurrent(Principal principal,
            HttpEntity<byte[]> requestEntity) throws Exception {  
        User user = (User)((Authentication) principal).getPrincipal();
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
        return payload.length;
    }

}