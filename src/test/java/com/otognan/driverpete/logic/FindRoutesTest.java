package com.otognan.driverpete.logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;
import com.otognan.driverpete.logic.endpoints.FindEndpointsProcessor;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterUtils;
import com.otognan.driverpete.logic.routes.RoutesFinder;


public class FindRoutesTest extends BaseStatelesSecurityITTest{
    
    @Autowired
    private AWSCredentials awsCredentials;
    
    private List<Location> testData;
        
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        if (this.testData == null) {
            AmazonS3 s3client = new AmazonS3Client(awsCredentials);
            S3Object object = s3client.getObject(
                    new GetObjectRequest("driverpete-storage", "_testing/testing_merged_1"));
            InputStream objectData = object.getObjectContent();
            
            List<Location> data = TrajectoryReader.readTrajectory(objectData);
            objectData.close();
            this.testData = TrajectoryFilterUtils.filterGPSData(data);
        }
    }
    
    @Test
    public void testFindEndpoints() {
        List<Location> endpoints = FindEndpointsProcessor.findEndpoints(this.testData);
        
        assertEquals(2, endpoints.size());
        assertThat(this.testData.get(478), equalTo(endpoints.get(0)));
        assertThat(this.testData.get(669), equalTo(endpoints.get(1)));
    }  
    
    @Test
    public void testFindRoutes() throws Exception {
        List<Location> endpoints = FindEndpointsProcessor.findEndpoints(this.testData);
        RoutesFinder finder = new RoutesFinder(endpoints);
        
        for (Location point : this.testData) {
            finder.process(point);
        }
        
        List<List<Location>> AtoBPaths = finder.getAtoBRoutes();
        List<List<Location>> BtoAPaths = finder.getBtoARoutes();
  
        int[][] AtoBPathsIndices = extractPathsIndices(this.testData, AtoBPaths);
        int[][] BtoAPathsIndices = extractPathsIndices(this.testData, BtoAPaths);

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
