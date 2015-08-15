package com.otognan.driverpete.logic;

public class StationaryPointsFilter implements TrajectoryFilter{
    
    private double distanceThreshold;

    public StationaryPointsFilter(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }
    
    public StationaryPointsFilter() {
        this(1.);
    }
    
    @Override
    public boolean allow(Location current_p, Location next_p) {
        if (Location.distance(current_p, next_p) < this.distanceThreshold) {
            return false;
        }
        return true;
    }

}
