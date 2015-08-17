package com.otognan.driverpete.logic.endpoints;

import java.util.ArrayList;
import java.util.List;

import com.otognan.driverpete.logic.Location;

public class FindEndpointsProcessor {
    private double stationaryThreshold;
    private double endpointsDistance;
    
    private List<Location> endpoints;
    
    private Location previousPoint = null;

    public FindEndpointsProcessor(List<Location> endpoints,
            double stationaryThreshold, double endpointsDistance) {
        this.endpoints = endpoints;
        this.stationaryThreshold = stationaryThreshold;
        this.endpointsDistance = endpointsDistance;
    }
    
    public FindEndpointsProcessor(List<Location> endpoints) {
        this(endpoints, (60*60)*3, 1000.);
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
        }
        this.previousPoint = point;
    }
    
    private boolean endpointExists(Location point) {
        for (Location l: this.endpoints) {
            if (Location.distance(l, point) < this.endpointsDistance) {
                return true;
            }
        }
        return false;
    }
        
    public Location getPreviousPoint() {
        return previousPoint;
    }

    public void setPreviousPoint(Location previousPoint) {
        this.previousPoint = previousPoint;
    }
    
    static public List<Location> findEndpoints(List<Location> data) {
        List<Location> endpointsContainer = new ArrayList<Location>();
        FindEndpointsProcessor processor = new FindEndpointsProcessor(endpointsContainer);
        for (Location location : data) {
            processor.process(location);
        }
        return endpointsContainer;
    }
}
