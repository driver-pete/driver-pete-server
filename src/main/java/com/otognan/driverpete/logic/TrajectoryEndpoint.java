package com.otognan.driverpete.logic;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.otognan.driverpete.security.User;


@Entity
@Table(name = "trajectory_endpoint")
public class TrajectoryEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private float latitude;
    
    private float longitude;
    
    private String label;
    
    @ManyToOne
    private User user;
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
