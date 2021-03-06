package com.otognan.driverpete.logic.endpoints;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.otognan.driverpete.logic.Location;
import com.otognan.driverpete.security.User;


@Entity
@Table(name = "endpoint_processor_state")
public class EndpointProcessorState {
    @Id
    private Long userId;
    
    private Location processorPreviousPoint;

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
