package com.otognan.driverpete.logic;

import java.util.ArrayList;
import java.util.List;

public class RoutesFinder {
    
    private List<List<Location>> AtoBRoutes = new ArrayList<List<Location>>();
    private List<List<Location>> BtoARoutes = new ArrayList<List<Location>>();
    private List<Location> currentRoute = new ArrayList<Location>();
    private List<Location> endpoints;
    private double distanceToStartRoute;
    private double continuityThreshold;
    private Integer fromEndpointIndex;
    
    public RoutesFinder(List<Location> endpoints, double distanceToStartRoute, double continuityThreshold) throws Exception
    {
        if (endpoints.size() != 2) {
            throw new Exception("Exactly 2 endpoints are expected");
        }
        this.endpoints = endpoints;
        this.distanceToStartRoute = distanceToStartRoute;
        this.continuityThreshold = continuityThreshold;
        this.fromEndpointIndex = null;
    }
    
    public RoutesFinder(List<Location> endpoints) throws Exception {
        this(endpoints, 200, 60*10);
    }
    
    private void startRoute(Location location, Integer endpointIndex) {
        this.fromEndpointIndex = endpointIndex;
        this.currentRoute.add(location);
    }
    
    private Integer toEndpointIndex() {
        return (this.fromEndpointIndex + 1) % 2;
    }
    
    private void stopRoute() {
        this.currentRoute = new ArrayList<Location>();
        this.fromEndpointIndex = null;
    }
    
    private Integer closestEndpointIndex(Location point) {
        for (int i = 0; i < this.endpoints.size(); i++) {
            if (Location.distance(point, this.endpoints.get(i)) < this.distanceToStartRoute) {
                return i;
            }
        }
        return null;
    }
    
    public void process(Location point) {
        if (this.fromEndpointIndex == null) {
            Integer index = this.closestEndpointIndex(point);
            if (index != null) {
                this.startRoute(point, index);
            }
            return;
        } else {
            double dt = Location.deltaTime(
                    this.currentRoute.get(this.currentRoute.size()-1), point);
            if (dt > this.continuityThreshold) {
                this.stopRoute();
                return;
            }
            
            this.currentRoute.add(point);
            Integer index = this.closestEndpointIndex(point);
            if (index == this.fromEndpointIndex) {
                this.stopRoute();
                this.startRoute(point, index);
            } else if (index == this.toEndpointIndex()) {
                if (this.fromEndpointIndex == 0) {
                    this.AtoBRoutes.add(this.currentRoute);
                    this.stopRoute();
                } else {
                    this.BtoARoutes.add(this.currentRoute);
                    this.stopRoute();
                }
            }
        }
    }
    
    
    public List<List<Location>> getAtoBRoutes() {
        return this.AtoBRoutes;
    }
    
    public List<List<Location>> getBtoARoutes() {
        return this.BtoARoutes;
    }
}
