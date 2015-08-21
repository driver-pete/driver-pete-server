package com.otognan.driverpete.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


@Service
public class TrajectoryDownloadService {
    static private final String BUCKET_NAME = "driverpete-storage";
    
    @Autowired
    AWSCredentials awsCredentials;
    
    public byte[] downloadBinaryTrajectory(String key) throws IOException, ParseException {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        S3Object object = s3client.getObject(
                new GetObjectRequest(BUCKET_NAME, key));
        InputStream objectData = object.getObjectContent();
        byte[] result = IOUtils.toByteArray(objectData);
        objectData.close();
        return result;
    }
        
    public List<Location> downloadTrajectory(String key) throws IOException, ParseException {
        byte[] binaryTrajectory = downloadBinaryTrajectory(key);
        return TrajectoryReader.readTrajectory(binaryTrajectory);
    }
    
    public void uploadBinaryTrajectory(String key, byte[] binaryTrajectory) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        InputStream dataStream = new ByteArrayInputStream(binaryTrajectory);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(binaryTrajectory.length);
        s3client.putObject(new PutObjectRequest(BUCKET_NAME, key, dataStream, meta));
    }
    
    public void uploadTrajectory(String key, List<Location> trajectory) throws Exception {
        byte[] binaryTrajectory = TrajectoryReader.writeTrajectory(trajectory);
        uploadBinaryTrajectory(key, binaryTrajectory);
    }
    
    public void deleteTrajectory(String key) {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        s3client.deleteObject(BUCKET_NAME, key);
    }
    
    public void copyTrajectory(String fromKey, String toKey) {
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        s3client.copyObject(BUCKET_NAME, fromKey, BUCKET_NAME, toKey);
    }
}
