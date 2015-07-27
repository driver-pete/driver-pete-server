package com.otognan.driverpete.logic;


import retrofit.http.GET;

public interface TrajectoryLogicApi {
    @GET("/api/trajectory/hello")
    String trajectoryHello();
}
    