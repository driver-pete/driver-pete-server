package com.otognan.driverpete.logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.otognan.driverpete.BaseStatelesSecurityITTest;
import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.logic.filtering.TrajectoryFilterUtils;
import com.otognan.driverpete.logic.routes.Route;

@Transactional
public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {

    @Autowired
    private AWSCredentials awsCredentials;

    @Autowired
    private TrajectoryDownloadService downloadService;

    private byte[] __trajectoryBytes;

    private TrajectoryLogicApi server() throws Exception {
        String token = this.getTestToken();
        return this.serverAPI(token, TrajectoryLogicApi.class);
    }
    
    @Before
    public void cleanTheStateBefore() throws Exception {
        this.server().deleteAllUserData();
    }

    @After
    public void cleanTheState() throws Exception {
        this.server().deleteAllUserData();
        System.out.println("--------------Cleared all the state-----------------");
    }

    private byte[] getStandardTrajectoryBytes() throws IOException,
            ParseException {
        if (this.__trajectoryBytes == null) {
            this.__trajectoryBytes = downloadService
                    .downloadBinaryTrajectory("_testing/testing_merged_1");
        }
        return this.__trajectoryBytes;
    }

    private String generateTrajectoryName() {
        return Location.dateToString(System.currentTimeMillis());
    }

    @Test
    public void uploadToS3SucceedsEvenIfBadData() throws Exception {
        /*
         * Its important during development that even bad data in case of bug in
         * the client goes through, because otherwise it would be lost.
         */
        String inputStr = "Hello. I'm not a trajectory";
        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes());
        TypedInput in = new TypedByteArray("application/octet-stream",
                encodedBytes);

        String trajectoryName = this.generateTrajectoryName();
        try {
            this.server().compressed(trajectoryName, in);
        } catch (RetrofitError ex) {
            // expected error
        }

        // Check that file is there
        AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

        String uploadedKey = "TestMike/data/" + trajectoryName;

        S3Object object = s3Client.getObject(new GetObjectRequest(
                "driverpete-storage", uploadedKey));

        String outputStr = IOUtils.toString(new InputStreamReader(object
                .getObjectContent()));

        s3Client.deleteObject("driverpete-storage", uploadedKey);

        assertThat(inputStr, equalTo(outputStr));
    }

    @Test
    public void findEndpoints() throws Exception {
        byte[] trajectoryBytes = this.getStandardTrajectoryBytes();
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);

        String trajectoryName = this.generateTrajectoryName();
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));

        List<TrajectoryEndpoint> endpoints = this.server()
                .trajectoryEndpoints();

        assertThat(endpoints.size(), equalTo(2));

        List<Location> data = TrajectoryReader.readTrajectory(trajectoryBytes);
        data = TrajectoryFilterUtils.filterGPSData(data);

        assertThat(data.get(478).getLatitude(), equalTo(endpoints.get(0)
                .getLatitude()));
        assertThat(data.get(478).getLongitude(), equalTo(endpoints.get(0)
                .getLongitude()));

        assertThat(data.get(669).getLatitude(), equalTo(endpoints.get(1)
                .getLatitude()));
        assertThat(data.get(669).getLongitude(), equalTo(endpoints.get(1)
                .getLongitude()));
    }

    @Test
    public void findRoutes() throws Exception {
        byte[] trajectoryBytes = this.getStandardTrajectoryBytes();
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);

        String trajectoryName = this.generateTrajectoryName();
        ;
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));

        List<List<Location>> routesAtoB = getAllRoutes(true);
        List<List<Location>> routesBtoA = getAllRoutes(false);

        assertThat(routesAtoB.size(), equalTo(6));
        assertThat(routesBtoA.size(), equalTo(6));

        List<Location> data = TrajectoryReader.readTrajectory(trajectoryBytes);
        data = TrajectoryFilterUtils.filterGPSData(data);

        int[][] AtoBPathsIndices = extractPathsIndices(data, routesAtoB);
        int[][] BtoAPathsIndices = extractPathsIndices(data, routesBtoA);

        int expectedAtoBIndices[][] = { { 485, 659 }, { 944, 1121 },
                { 1358, 1552 }, { 2210, 2403 }, { 2624, 2900 }, { 4379, 4509 } };

        int expectedBtoAIndices[][] = { { 124, 456 }, { 678, 893 },
                { 1137, 1317 }, { 1570, 1784 }, { 2423, 2596 }, { 3957, 4158 } };
        assertThat(AtoBPathsIndices, equalTo(expectedAtoBIndices));
        assertThat(BtoAPathsIndices, equalTo(expectedBtoAIndices));
    }

    @Test
    public void findEndpointsWithState() throws Exception {
        byte[] trajectoryBytes = this.getStandardTrajectoryBytes();

        List<Location> fullTrajectory = TrajectoryReader
                .readTrajectory(trajectoryBytes);
        List<List<Location>> pieces = new ArrayList<List<Location>>();
        pieces.add(fullTrajectory.subList(0, 480));
        pieces.add(fullTrajectory.subList(480, fullTrajectory.size()));

        for (List<Location> piece : pieces) {
            byte[] pieceBytes = TrajectoryReader.writeTrajectory(piece);
            byte[] base64Bytes = Base64.encodeBase64(pieceBytes);

            this.server().compressed(
                            this.generateTrajectoryName(),
                            new TypedByteArray("application/octet-stream",
                                    base64Bytes));
            // sleep so that trajectory names are different
            Thread.sleep(1100);
        }

        List<TrajectoryEndpoint> endpoints = this.server()
                .trajectoryEndpoints();

        assertThat(endpoints.size(), equalTo(2));

        List<Location> data = TrajectoryReader.readTrajectory(trajectoryBytes);
        data = TrajectoryFilterUtils.filterGPSData(data);

        assertThat(data.get(478).getLatitude(), equalTo(endpoints.get(0)
                .getLatitude()));
        assertThat(data.get(478).getLongitude(), equalTo(endpoints.get(0)
                .getLongitude()));

        assertThat(data.get(669).getLatitude(), equalTo(endpoints.get(1)
                .getLatitude()));
        assertThat(data.get(669).getLongitude(), equalTo(endpoints.get(1)
                .getLongitude()));
    }

    @Test
    public void findRoutesWithState() throws Exception {
        byte[] trajectoryBytes = this.getStandardTrajectoryBytes();

        List<Location> fullTrajectory = TrajectoryReader
                .readTrajectory(trajectoryBytes);
        List<List<Location>> pieces = new ArrayList<List<Location>>();
        pieces.add(fullTrajectory.subList(0, 50));
        pieces.add(fullTrajectory.subList(50, 130));
        pieces.add(fullTrajectory.subList(130, 200));
        pieces.add(fullTrajectory.subList(200, 480));
        pieces.add(fullTrajectory.subList(480, 2300));
        pieces.add(fullTrajectory.subList(2300, 3000));
        pieces.add(fullTrajectory.subList(3000, 4000));
        pieces.add(fullTrajectory.subList(4000, fullTrajectory.size()));

        for (List<Location> piece : pieces) {
            byte[] pieceBytes = TrajectoryReader.writeTrajectory(piece);
            byte[] base64Bytes = Base64.encodeBase64(pieceBytes);

            this.server().compressed(
                            this.generateTrajectoryName(),
                            new TypedByteArray("application/octet-stream",
                                    base64Bytes));
            // sleep so that trajectory names are different
            Thread.sleep(1100);
        }

        List<Location> data = TrajectoryReader.readTrajectory(trajectoryBytes);
        data = TrajectoryFilterUtils.filterGPSData(data);
        
        int expectedAtoBIndices[][] = { { 485, 659 }, { 944, 1121 },
                { 1358, 1552 }, { 2210, 2403 }, { 2624, 2900 }, { 4379, 4509 } };

        int expectedBtoAIndices[][] = { { 124, 456 }, { 678, 893 },
                { 1137, 1317 }, { 1570, 1784 }, { 2423, 2596 }, { 3957, 4158 } };
        
        checkRoute(data, true, 6, expectedAtoBIndices);
        checkRoute(data, false, 6, expectedBtoAIndices);
        
        this.server().resetProcessorState();
        this.server().deleteAllEndpoints();
        this.server().deleteAllRoutes();
        
        // now check that reprocessing gives the same results
        this.server().reprocessAllUserData(true);
        
        checkRoute(data, true, 6, expectedAtoBIndices);
        checkRoute(data, false, 6, expectedBtoAIndices);
    }

    private int[] pathIndices(List<Location> data, List<Location> path) {
        int indices[] = { data.indexOf(path.get(0)),
                data.indexOf(path.get(path.size() - 1)) };
        return indices;
    }

    private int[][] extractPathsIndices(List<Location> data,
            List<List<Location>> paths) {
        int result[][] = new int[paths.size()][2];
        for (int i = 0; i < paths.size(); i++) {
            result[i] = this.pathIndices(data, paths.get(i));
        }
        return result;
    }
    
    private void checkRoute(List<Location> data, boolean isAtoB, int expectedSize, int[][] expectedAtoBIndices) throws Exception {
        List<List<Location>> routes= getAllRoutes(isAtoB);

        assertThat(routes.size(), equalTo(expectedSize));

        int[][] pathsIndices = extractPathsIndices(data, routes);

        assertThat(pathsIndices, equalTo(expectedAtoBIndices));
    }

    @Test
    public void editEndpoints() throws Exception {
        List<Location> longTrajectory = TrajectoryReader.readTrajectory(this.getStandardTrajectoryBytes());
        byte[] trajectoryBytes = TrajectoryReader.writeTrajectory(longTrajectory.subList(0, 1000));
        
        byte[] base64Bytes = Base64.encodeBase64(trajectoryBytes);
        String trajectoryName = this.generateTrajectoryName();
        this.server().compressed(trajectoryName,
                new TypedByteArray("application/octet-stream", base64Bytes));

        List<TrajectoryEndpoint> endpoints = this.server().trajectoryEndpoints();
        assertThat(endpoints.size(), equalTo(2));
        TrajectoryEndpoint a = endpoints.get(0);
        TrajectoryEndpoint b = endpoints.get(1);
        
        checkEndpoints("A", "3661 Valley Centre Drive, San Diego, CA 92130, USA", 
                "B", "15992 Avenida Villaha, San Diego, CA 92128, USA");
    
        b.setLabel("Home");
        b.setAddress("My address");
        this.server().editEndpoint(b);
        
        checkEndpoints("A", "3661 Valley Centre Drive, San Diego, CA 92130, USA", 
                "Home", "My address");

        a.setLabel("Work");
        a.setAddress("Another address");
        this.server().editEndpoint(a);
        
        checkEndpoints("Work", "Another address", "Home", "My address");
        
        TrajectoryEndpoint newa = new TrajectoryEndpoint();
        newa.setId(a.getId());
        newa.setLabel("Work 2");
        newa.setAddress(a.getAddress());
        this.server().editEndpoint(newa);
        
        checkEndpoints("Work 2", "Another address", "Home", "My address");
        
        TrajectoryEndpoint badid = new TrajectoryEndpoint();
        badid.setId(a.getId() + 1000);
        badid.setLabel("Work 3");
        badid.setAddress(a.getAddress());
        try {
            this.server().editEndpoint(badid);
            fail("Exception not thrown");
        } catch (RetrofitError error) {
            assertThat(error.getResponse().getStatus(), equalTo(403));
        }
        
        checkEndpoints("Work 2", "Another address", "Home", "My address");
        
        TrajectoryEndpoint noid = new TrajectoryEndpoint();
        noid.setLabel("Work 3");
        noid.setAddress(a.getAddress());
        try {
            this.server().editEndpoint(noid);
            fail("Exception not thrown");
        } catch (RetrofitError error) {
            assertThat(error.getResponse().getStatus(), equalTo(403));
        }
        
        checkEndpoints("Work 2", "Another address", "Home", "My address");
    }
    
    private void checkEndpoints(String expectedLabelA, String expectedAddressA,
            String expectedLabelB, String expectedAddressB) throws Exception {
        List<TrajectoryEndpoint> endpoints = this.server().trajectoryEndpoints();
        assertThat(endpoints.size(), equalTo(2));
        TrajectoryEndpoint a = endpoints.get(0);
        TrajectoryEndpoint b = endpoints.get(1);
        assertThat(a.getLabel(), equalTo(expectedLabelA));
        assertThat(b.getLabel(), equalTo(expectedLabelB));
        assertThat(a.getAddress(), equalTo(expectedAddressA));
        assertThat(b.getAddress(), equalTo(expectedAddressB));
    }
    
    private List<List<Location>> getAllRoutes(boolean isAtoB) throws Exception {
        List<List<Location>> routes = new ArrayList<List<Location>>();
        for (Route routeEntity : this.server().routes(isAtoB)) {
            TypedByteArray body = (TypedByteArray)this.server().binaryRoute(routeEntity.getId()).getBody();
            List<Location> route = TrajectoryReader.readTrajectory(Base64
                    .decodeBase64(body.getBytes()));
            routes.add(route);
        }
        return routes;
    }
    
}
