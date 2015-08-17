package com.otognan.driverpete.logic.filtering;

import com.otognan.driverpete.logic.Location;


public interface TrajectoryFilter {
    public boolean allow(Location current_p, Location next_p);
}
