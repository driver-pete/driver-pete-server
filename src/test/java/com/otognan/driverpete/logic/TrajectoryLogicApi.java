package com.otognan.driverpete.logic;


import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.mime.TypedInput;

public interface TrajectoryLogicApi {
    @POST("/api/trajectory/compressed")
    int compressed(@Body TypedInput body);
}
    