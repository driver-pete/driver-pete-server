package com.otognan.driverpete.logic.routes;

import java.util.ArrayList;
import java.util.List;

import com.otognan.driverpete.logic.Location;

public class RoutesFinder {
    
    private List<List<Location>> AtoBRoutes = new ArrayList<List<Location>>();
    private List<List<Location>> BtoARoutes = new ArrayList<List<Location>>();
    private List<Location> endpoints;
    private double distanceToStartRoute;
    private double continuityThreshold;
    
    // Current state
    private List<Location> currentRoute = new ArrayList<Location>();
    private int fromEndpointIndex = -1;
    
    public RoutesFinder(List<Location> endpoints, double distanceToStartRoute, double continuityThreshold) throws Exception
    {
        if (endpoints.size() != 2) {
            throw new Exception("Exactly 2 endpoints are expected");
        }
        this.endpoints = endpoints;
        this.distanceToStartRoute = distanceToStartRoute;
        this.continuityThreshold = continuityThreshold;
        this.fromEndpointIndex = -1;
    }
    
    public RoutesFinder(List<Location> endpoints) throws Exception {
        this(endpoints, 200, 60*10);
    }
    
    private void startRoute(Location location, int endpointIndex) {
        this.fromEndpointIndex = endpointIndex;
        this.currentRoute.add(location);
    }
    
    private int toEndpointIndex() {
        return (this.fromEndpointIndex + 1) % 2;
    }
    
    private void stopRoute() {
        this.currentRoute = new ArrayList<Location>();
        this.fromEndpointIndex = -1;
    }
    
    private int closestEndpointIndex(Location point) {
        for (int i = 0; i < this.endpoints.size(); i++) {
            if (Location.distance(point, this.endpoints.get(i)) < this.distanceToStartRoute) {
                return i;
            }
        }
        return -1;
    }
    
    public void process(Location point) {
        if (this.fromEndpointIndex == -1) {
            int index = this.closestEndpointIndex(point);
            if (index != -1) {
                this.startRoute(point, index);
            }
            return;
        } else {
            double dt = Location.deltaTime(
                    this.currentRoute.get(this.currentRoute.size()-1), point);
            if (dt > this.continuityThreshold) {
                int index = this.closestEndpointIndex(point);
                if (index == this.fromEndpointIndex) {
                    this.stopRoute();
                    // We didnt move too far from beginning, so we start the route there
                    this.startRoute(point, index);
                } else {
                    this.stopRoute();
                }
                return;
            }
            
            this.currentRoute.add(point);
            int index = this.closestEndpointIndex(point);
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
    
    // state getters and setters
    public List<Location> getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(List<Location> currentRoute) {
        this.currentRoute = currentRoute;
    }

    public int getFromEndpointIndex() {
        return fromEndpointIndex;
    }

    public void setFromEndpointIndex(int fromEndpointIndex) {
        this.fromEndpointIndex = fromEndpointIndex;
    }
}
