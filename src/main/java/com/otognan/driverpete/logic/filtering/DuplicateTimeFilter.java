package com.otognan.driverpete.logic.filtering;

import com.otognan.driverpete.logic.Location;

public class DuplicateTimeFilter implements TrajectoryFilter {
    @Override
    public boolean allow(Location current_p, Location next_p) {
        double dt = Location.deltaTime(current_p, next_p);
        if (dt < 1) {
            return false;
        }
        return true;
    }
}
