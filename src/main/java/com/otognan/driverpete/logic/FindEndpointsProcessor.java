package com.otognan.driverpete.logic;

import java.util.ArrayList;
import java.util.List;

public class FindEndpointsProcessor {
    private double stationaryThreshold;
    private double endpointsDistance;
    
    private List<Location> endpoints = new ArrayList<Location>();
    private List<Location> newEndpoints = new ArrayList<Location>();
    
    private Location previousPoint = null;

    public FindEndpointsProcessor(double stationaryThreshold, double endpointsDistance) {
        this.stationaryThreshold = stationaryThreshold;
        this.endpointsDistance = endpointsDistance;
    }
    
    public FindEndpointsProcessor() {
        this((60*60)*3, 1000.);
    }
    
    public void process(Location point) {
        if (this.previousPoint == null) {
            this.previousPoint = point;
            return;
        }
        
        double dt = Location.deltaTime(this.previousPoint, point);
        if (dt > this.stationaryThreshold &&
                !this.endpointExists(this.previousPoint)) {
            this.endpoints.add(this.previousPoint);
            this.newEndpoints.add(this.previousPoint);
        }
        this.previousPoint = point;
    }
    
    public List<Location> getNewEndpoints() {
        return this.newEndpoints;
    }

    public List<Location> getEndpoints() {
        return this.endpoints;
    }
    
    public void setEndpoints(List<Location> endpoints) {
        this.endpoints = endpoints;
    }
    
    private boolean endpointExists(Location point) {
        for (Location l: this.endpoints) {
            if (Location.distance(l, point) < this.endpointsDistance) {
                return true;
            }
        }
        return false;
    }
    
    static public List<Location> findEndpoints(List<Location> data) {
        FindEndpointsProcessor processor = new FindEndpointsProcessor();
        for (Location location : data) {
            processor.process(location);
        }
        return processor.getEndpoints();
    }
    
    public Location getPreviousPoint() {
        return previousPoint;
    }

    public void setPreviousPoint(Location previousPoint) {
        this.previousPoint = previousPoint;
    }
}
