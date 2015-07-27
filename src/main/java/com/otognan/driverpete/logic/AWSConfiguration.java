package com.otognan.driverpete.logic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;


@Configuration
public class AWSConfiguration {
    
    @Value("${AWS_ACCESS_KEY_ID}")
    private String amazonAWSAccessKey;

    @Value("${AWS_SECRET_KEY}")
    private String amazonAWSSecretKey;
    
    @Bean
    public AWSCredentials amazonAWSCredentials() {
        // Beanstalk defines empty keys by default, so we better check it here
        if(amazonAWSAccessKey == null || amazonAWSAccessKey.isEmpty()) {
            throw new IllegalArgumentException("AWSAccessKey is empty or null");
        }
        if(amazonAWSSecretKey == null || amazonAWSSecretKey.isEmpty()) {
            throw new IllegalArgumentException("AWSSecretKey is empty or null");
        }
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }
}