package com.otognan.driverpete.logic;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.security.User;

@Component
public class TrajectoryEndpointWorker {
    
    @Autowired
    AWSCredentials awsCredentials;
    

}
