package com.otognan.driverpete.logic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {
    
    private TrajectoryLogicApi server() throws Exception {
        String token = this.getTestToken();
        return this.serverAPI(token, TrajectoryLogicApi.class);
    }

    @Test
    public void getTrajectorySecuredAsUser() throws Exception {
        String response = this.server().trajectoryHello();
        assertThat(response, equalTo("HELLO"));
    }
    
}
