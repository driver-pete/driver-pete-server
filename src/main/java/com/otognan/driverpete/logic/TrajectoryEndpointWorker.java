package com.otognan.driverpete.logic;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TrajectoryEndpointWorker {
    
    @Async
    public void printThings(String whatToPrint) {
        for(int i=0; i<20; i++) {
            long threadId = Thread.currentThread().getId();
            System.out.println(whatToPrint + i + " " + String.valueOf(threadId));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @Async
    public void findEndpoints(String trajectoryKey) {
        
        //download
        //read 
        
        // create filters
        DuplicateTimeFilter duplicateTime = new DuplicateTimeFilter();
        StationaryPointsFilter stationaryPoint = new StationaryPointsFilter();
        VelocityOutliersFilter velocityOutlier = new VelocityOutliersFilter(85.);
        FindEndpointsProcessor processor = new FindEndpointsProcessor();
        
        // get user state for endpoints
        // put state into filters
        
        // do filtering
        // get endpoitns
        
        // determine if need to upload endpoints
        
        // AS A TRANSACTION
        // read state and ednpoints, determine that the same as before,
        // determine that trajectory key still exists
        // upload endpoints and new state
        // if successful, 
        // remove trajectoryKey (or copy to "processed")
        // FINISH TRANSACTION
        
    }
}
