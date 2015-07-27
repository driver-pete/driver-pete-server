package com.otognan.driverpete.logic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {

    @Test
    public void getTrajectorySecuredAsUser() throws Exception {
        String token = this.getTestToken();
        String response = this.requestWithToken(token,
                this.basePath + "/api/trajectory/hello", String.class).getBody();
        assertThat(response, equalTo("HELLO"));
    }
    
}
