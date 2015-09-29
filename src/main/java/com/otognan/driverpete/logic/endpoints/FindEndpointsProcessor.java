package com.otognan.driverpete.logic.endpoints;

import java.util.ArrayList;
import java.util.List;

import com.otognan.driverpete.logic.Location;

public class FindEndpointsProcessor {
    private double stationaryThreshold;
    private double endpointsDistance;
    private double stationaryDistanceThreshold;
    
    private List<Location> endpoints;
    
    private Location previousPoint = null;
    private Location hypothesisPoint = null;
    private int currentCumulativeDt = 0;

    public FindEndpointsProcessor(List<Location> endpoints,
            double stationaryThreshold, double endpointsDistance,
            double stationaryDistanceThreshold) {
        this.endpoints = endpoints;
        this.stationaryThreshold = stationaryThreshold;
        this.endpointsDistance = endpointsDistance;
        this.stationaryDistanceThreshold = stationaryDistanceThreshold;
    }
    
    public FindEndpointsProcessor(List<Location> endpoints) {
        this(endpoints, (60*60)*5, 1000, 500);
    }
    
    public void process(Location point) {
        if (this.previousPoint == null) {
            this.previousPoint = point;
            return;
        }
        
        // if there is no hypothesis, lets see if we far away from the existing endpoint
        if (this.hypothesisPoint == null) {
            if (!this.endpointExists(this.previousPoint)) {
                this.hypothesisPoint = this.previousPoint;
            } else {
                this.previousPoint = point;
                return;
            }
        }
        
        if (Location.distance(this.hypothesisPoint, this.previousPoint) < this.stationaryDistanceThreshold) {
            double dt = Location.deltaTime(this.previousPoint, point);
            // if we time contribution is large, then shift hypothesis point to the most recent onec
            if (dt > this.currentCumulativeDt) {
                this.hypothesisPoint = this.previousPoint;
            }
            this.currentCumulativeDt += dt;
            if (this.currentCumulativeDt > this.stationaryThreshold) {
                this.endpoints.add(this.hypothesisPoint);
                this.hypothesisPoint = null;
                this.currentCumulativeDt = 0;
            }
        } else {
            // moved too far from hypothesis point, reseting
            this.hypothesisPoint = null;
            this.currentCumulativeDt = 0;
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
    
    public Location getHypothesisPoint() {
        return hypothesisPoint;
    }

    public void setHypothesisPoint(Location hypothesisPoint) {
        this.hypothesisPoint = hypothesisPoint;
    }

    public int getCurrentCumulativeDt() {
        return currentCumulativeDt;
    }

    public void setCurrentCumulativeDt(int currentCumulativeDt) {
        this.currentCumulativeDt = currentCumulativeDt;
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
