package com.otognan.driverpete.logic;

public class VelocityOutliersFilter implements TrajectoryFilter {
    /*
     Typical bad gps samples look like this:
    i  time     long.         lat      dt   ds      v (mph)       
    1  t1     32.994390  -117.084058  6.0  134.3   75.
    2  t2     32.994390  -117.084058  4.0  0.0     0.0
    3  t3     32.991641  -117.083729e 5.0  306.4   171.
    
    Where dt - time from the previous sample, ds - distance from the previous sample in meters,
    v - current velocity based on the current and the previous sample.
    
    These readings were taken on the freeway. There is a sample that has the same long, lat. readings
    but different time. It looks like the car did not move at t2 and then suddenly jumped to location at t3.
    This leads to huge car velocity at the next sample.
    The solution is to just remove such samples from the data based on the big velocity (>thershold).
    thershold - velocity that is considered to be too big (in mph)
    
    Additionaly there might be large noise during imprecise stationary position.
    In this case measurements might come rarely but with huge noise (157km):
    ... SAN DIEGO
    22:30 - SAN DIEGO
    00:30 - LA
    00:33 - SAN DIEGO
    09:18 - LA
    09:19 - SAN DIEGO
    ... SAN DIEGO
    Time difference between samples is large enough to make velocity small. Moreover it is not obvious that it is
    noise at first - maybe a driver really drove to LA without taking measurements? However only later it becomes clear
    that LA measurements are noise.
    Algorithm takes preventive steps and ignores point that is too far from the previous one. This leads to a problem
    that if algorithm starts from the LA, it will never converge back to SD. Therefore there is a counter that doesn't
    allow algorithm to dismiss too many outliers in a row (there are not outliers in this case).
    */
    
    private double speedThreshold;
    private double distanceThreshold;
    private int maxNumberOutliers;
    private int outliersCounter;

    public VelocityOutliersFilter(double speedMphThershold, double distanceThreshold) {
        this.speedThreshold = speedMphThershold;
        this.distanceThreshold = distanceThreshold;
        
        this.maxNumberOutliers = 3;
        this.outliersCounter = this.maxNumberOutliers;
    }
    
    public VelocityOutliersFilter(double speedMphThershold) {
        this(speedMphThershold, 5000.);
    }
        
    public VelocityOutliersFilter() {
        this(85., 5000.);
    }

    @Override
    public boolean allow(Location current_p, Location next_p) {
        if (Location.velocityMph(current_p, next_p) > this.speedThreshold ||
                Location.distance(current_p, next_p) > this.distanceThreshold) {
            if (this.outliersCounter > 0) {
                this.outliersCounter -= 1;
                return false;
            }
        }
        this.outliersCounter = this.maxNumberOutliers;
        return true;
    }
}
