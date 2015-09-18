package com.otognan.driverpete.logic.endpoints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.otognan.driverpete.logic.Location;


@Entity
@Table(name = "endpoint_processor_state")
public class EndpointProcessorState {
    @Id
    private Long userId;
    
    private Location processorPreviousPoint;
    private Location processorHypothesisPoint;
    private int currentCumulativeDt;

    public Location getProcessorHypothesisPoint() {
        return processorHypothesisPoint;
    }

    public void setProcessorHypothesisPoint(Location processorHypothesisPoint) {
        this.processorHypothesisPoint = processorHypothesisPoint;
    }

    public int getCurrentCumulativeDt() {
        return currentCumulativeDt;
    }

    public void setCurrentCumulativeDt(int currentCumulativeDt) {
        this.currentCumulativeDt = currentCumulativeDt;
    }

    public Location getProcessorPreviousPoint() {
        return processorPreviousPoint;
    }

    public void setProcessorPreviousPoint(Location processorPreviousPoint) {
        this.processorPreviousPoint = processorPreviousPoint;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
