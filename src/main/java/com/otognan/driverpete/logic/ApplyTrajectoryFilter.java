package com.otognan.driverpete.logic;

import java.util.ArrayList;
import java.util.List;

public class ApplyTrajectoryFilter {
    static public List<Location> apply(List<Location> data, TrajectoryFilter filter) {
        Location prevPoint = data.get(0);
        List<Location> result = new ArrayList<Location>();
        result.add(prevPoint);

        for (int i=1; i<data.size(); i++) {
            if (filter.allow(prevPoint, data.get(i))) {
                prevPoint = data.get(i);
                result.add(data.get(i));
            }
        }
        return result;
    }
}
