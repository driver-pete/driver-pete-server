package com.otognan.driverpete.logic.filtering;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "filtering_state")
public class FilteringState {
    @Id
    private Long userId;
    
    private int velocityOutliersCounter;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getVelocityOutliersCounter() {
        return velocityOutliersCounter;
    }

    public void setVelocityOutliersCounter(int velocityOutliersCounter) {
        this.velocityOutliersCounter = velocityOutliersCounter;
    }
}
