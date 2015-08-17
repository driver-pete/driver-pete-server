package com.otognan.driverpete.logic;


import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedInput;

public interface TrajectoryLogicApi {
    @POST("/api/trajectory/compressed_length")
    int compressedLength(@Body TypedInput body);
 
    @POST("/api/trajectory/compressed")
    Response compressed(@Query("label") String label, @Body TypedInput body);
    
    @GET("/api/trajectory/endpoints")
    List<TrajectoryEndpoint> trajectoryEndpoints();
}
    