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
}
