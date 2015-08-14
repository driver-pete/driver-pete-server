package com.otognan.driverpete.logic;

public class DuplicateTimeFilter implements TrajectoryFilter {
    public boolean allow(Location current_p, Location next_p) {
        double dt = Location.deltaTime(current_p, next_p);
        if (dt < 1) {
            return false;
        }
        return true;
    }
}
