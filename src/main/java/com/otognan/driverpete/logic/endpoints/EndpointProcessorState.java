package com.otognan.driverpete.logic.endpoints;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.otognan.driverpete.logic.Location;


@Entity
@Table(name = "endpoint_processor_state")
public class EndpointProcessorState {
    @Id
    private Long userId;
    
    @Embedded 
    @AttributeOverrides({ 
        @AttributeOverride(name="time",column=@Column(name="prev_time")), 
        @AttributeOverride(name="latitude",column=@Column(name="prev_latitude")), 
        @AttributeOverride(name="longitude",column=@Column(name="prev_longitude")) 
    }) 
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
