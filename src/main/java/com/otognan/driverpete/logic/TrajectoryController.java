package com.otognan.driverpete.logic;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.security.User;


@RestController
public class TrajectoryController {
    
    @Autowired
    private TrajectoryService trajectoryService;
    
    // Method for testing of binary upload
    @RequestMapping(value = "/api/trajectory/compressed_length",
            method = RequestMethod.POST)
    public int compressedLength(HttpEntity<byte[]> requestEntity) throws Exception {  
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
        return payload.length;
    }
    
    @RequestMapping(value = "/api/trajectory/compressed",
            method = RequestMethod.POST)
    public void uploadTrajectory(Principal principal,
            @RequestParam("label") String label,
            HttpEntity<byte[]> requestEntity) throws Exception {  
        User user = (User)((Authentication)principal).getPrincipal(); 
        byte[] payload = Base64.decodeBase64(requestEntity.getBody());
        trajectoryService.processBinaryTrajectory(user, label, payload);
    }
    
    @RequestMapping(value = "/api/trajectory/endpoints", method = RequestMethod.GET)
    public List<TrajectoryEndpoint> trajectoryEndpoints(Principal principal) {
        User user = (User)((Authentication)principal).getPrincipal();
        return this.trajectoryService.getUserTrajectoryEndpoint(user);
    }

}