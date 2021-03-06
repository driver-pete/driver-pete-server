package com.otognan.driverpete.logic;


import java.util.List;

import com.otognan.driverpete.logic.endpoints.TrajectoryEndpoint;
import com.otognan.driverpete.logic.routes.Route;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedInput;

public interface TrajectoryLogicApi {
 
    @POST("/api/trajectory/compressed")
    public Response compressed(@Query("label") String label, @Body TypedInput body);
    
    @GET("/api/trajectory/endpoints")
    public List<TrajectoryEndpoint> trajectoryEndpoints();
    
    @POST("/api/trajectory/endpoints")
    public Response editEndpoint(@Body TrajectoryEndpoint endpoint);
    
    @GET("/api/trajectory/routes")
    public List<Route> routes(@Query("isAtoB") boolean isAtoB);
    
    @GET("/api/trajectory/routes/binary")
    public Response binaryRoute(@Query("routeId") long routeId);
    
    @DELETE("/api/trajectory/state")
    public Response resetProcessorState();
    
    @DELETE("/api/trajectory/endpoints/all")
    public Response deleteAllEndpoints();
    
    @DELETE("/api/trajectory/routes/all")
    public Response deleteAllRoutes();
    
    @DELETE("/api/trajectory/all")
    public Response deleteAllUserData();
    
    @GET("/api/trajectory/reprocess/all")
    public Response reprocessAllUserData(@Query("reprocessRoutes") boolean reprocessRoutes);
}
    