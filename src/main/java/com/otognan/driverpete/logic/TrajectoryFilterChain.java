package com.otognan.driverpete.logic;


public class TrajectoryFilterChain implements TrajectoryFilter{
    
    private TrajectoryFilter[] chain;

    public TrajectoryFilterChain(TrajectoryFilter[] chain) {
        this.chain = chain;
    }
    
    @Override
    public boolean allow(Location current_p, Location next_p) {
        for (TrajectoryFilter filter : chain) {
            if (!filter.allow(current_p, next_p)) return false;
        }
        return true;
}
}
