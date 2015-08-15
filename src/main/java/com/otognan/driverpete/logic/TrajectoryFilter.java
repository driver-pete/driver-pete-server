package com.otognan.driverpete.logic;


public interface TrajectoryFilter {
    public boolean allow(Location current_p, Location next_p);
}
