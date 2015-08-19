package com.otognan.driverpete.logic.routes;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "routes_state")
public class RoutesState {
    @Id
    private Long userId;
    
    private int fromEndpointIndex;
    
    private String currentRouteKey;
    
    public int getFromEndpointIndex() {
        return fromEndpointIndex;
    }

    public void setFromEndpointIndex(int fromEndpointIndex) {
        this.fromEndpointIndex = fromEndpointIndex;
    }

    public String getCurrentRouteKey() {
        return currentRouteKey;
    }

    public void setCurrentRouteKey(String currentRouteKey) {
        this.currentRouteKey = currentRouteKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "RoutesState [userId=" + userId + ", fromEndpointIndex="
                + fromEndpointIndex + ", currentRouteKey=" + currentRouteKey
                + "]";
    }    
}
