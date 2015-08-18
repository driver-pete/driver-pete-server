package com.otognan.driverpete.logic;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterUtils;


//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true) 
@Transactional
public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {
    
    @Autowired
    private AWSCredentials awsCredentials;
    
    @Autowired
    private TrajectoryDownloadService downloadService;
    
    private TrajectoryLogicApi server() throws Exception {
        String token = this.getTestToken();
        return this.serverAPI(token, TrajectoryLogicApi.class);
    }

//    @Test
//    public void determineDataLength() throws Exception {
//        String inputStr = "   &*()!@#$$%^&())((     ()/n/ndfgsd)(*)(@''''???";
//        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes("UTF-8"));
//        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
//        
//        int dataLenth = this.server().compressedLength(in);
//        
//        assertThat(dataLenth, equalTo(inputStr.length()));
//    }
//    
//    @Test
//    public void uploadToS3SucceedsEvenIfBadData() throws Exception {
//        /*
//         * Its important during development that even bad data in case of bug in the client
//         * goes through, because otherwise it would be lost.
//         */
//        String inputStr = "Hello. I'm not a trajectory";
//        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes());
//        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
//        
//        String trajectoryName = "_my_test_bad_trajectory";
//        try {
//            this.server().compressed(trajectoryName, in);
//        } catch (RetrofitError ex) {
//            // expected error
//        }
//        
//        // Check that file is there
//        AmazonS3 s3Client = new AmazonS3Client(awsCredentials);  
//        
//        String uploadedKey = "TestMike/" + trajectoryName;
//        
//        S3Object object = s3Client.getObject(new GetObjectRequest("driverpete-storage", uploadedKey));
//        
//        String outputStr = IOUtils.toString(new InputStreamReader(
//                object.getObjectContent()));
//        
//        s3Client.deleteObject("driverpete-storage", uploadedKey);
//        
//        assertThat(inputStr, equalTo(outputStr));
//    }
    
    
    @Test
    public void findEndpoints() throws Exception {
        byte[] trajectoryBytes = downloadService.downloadBinaryTrajectory("_testing/testing_merged_1");
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);
         
        String trajectoryName = "_my_trajectory";
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));
        
        List<TrajectoryEndpoint> endpoints = this.server().trajectoryEndpoints();
        
        assertThat(endpoints.size(), equalTo(2));
        
        List<Location> data = TrajectoryReader.readTrajectory(new ByteArrayInputStream(trajectoryBytes));
        data = TrajectoryFilterUtils.filterGPSData(data);
                
        assertThat(data.get(478).getLatitude(), equalTo(endpoints.get(0).getLatitude()));
        assertThat(data.get(478).getLongitude(), equalTo(endpoints.get(0).getLongitude()));
       
        assertThat(data.get(669).getLatitude(), equalTo(endpoints.get(1).getLatitude()));
        assertThat(data.get(669).getLongitude(), equalTo(endpoints.get(1).getLongitude()));
    }
    
    @Test
    public void findRoutes() throws Exception {
        
        System.out.println("________________________________________________");
        
        this.server().resetProcessorState();
        this.server().deleteAllEndpoints();
        
        byte[] trajectoryBytes = downloadService.downloadBinaryTrajectory("_testing/testing_merged_1");
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);
         
        String trajectoryName = "_my_trajectory";
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));
        
        List<String> binRoutesAtoB = this.server().routes(true);
        List<String> binRoutesBtoA = this.server().routes(false);
        
        List<List<Location>> routesAtoB = new ArrayList<List<Location>>();
        for (String binRoute: binRoutesAtoB) {
            List<Location> route = TrajectoryReader.readTrajectory(
                    new ByteArrayInputStream(Base64.decodeBase64(binRoute.getBytes())));
            routesAtoB.add(route);
        }
        
        List<List<Location>> routesBtoA = new ArrayList<List<Location>>();
        for (String binRoute: binRoutesBtoA) {
            List<Location> route = TrajectoryReader.readTrajectory(
                    new ByteArrayInputStream(Base64.decodeBase64(binRoute.getBytes())));
            routesBtoA.add(route);
        }
        
        assertThat(routesAtoB.size(), equalTo(6));
        assertThat(routesBtoA.size(), equalTo(6));
        
        List<Location> data = TrajectoryReader.readTrajectory(new ByteArrayInputStream(trajectoryBytes));
        data = TrajectoryFilterUtils.filterGPSData(data);
        
        int[][] AtoBPathsIndices = extractPathsIndices(data, routesAtoB);
        int[][] BtoAPathsIndices = extractPathsIndices(data, routesBtoA);

        int expectedAtoBIndices[][] = {
                {485, 659}, {944, 1121}, {1358, 1552}, {2210, 2403}, {2624, 2900}, {4379, 4509}};

        int expectedBtoAIndices[][] = {
                {124, 456}, {678, 893}, {1137, 1317}, {1570, 1784}, {2423, 2596}, {3957, 4158}};
        assertThat(AtoBPathsIndices, equalTo(expectedAtoBIndices));
        assertThat(BtoAPathsIndices, equalTo(expectedBtoAIndices));
    }
    
    private int[] pathIndices(List<Location> data, List<Location> path) {
        int indices[] = {data.indexOf(path.get(0)),
                data.indexOf(path.get(path.size()-1))};
        return indices;
    }
    
    private int[][] extractPathsIndices(List<Location> data, List<List<Location>> paths) {
        int result[][] = new int[paths.size()][2];
        for (int i = 0; i < paths.size(); i++) {
            result[i] = this.pathIndices(data, paths.get(i));
        }
        return result;
    }
    
}
