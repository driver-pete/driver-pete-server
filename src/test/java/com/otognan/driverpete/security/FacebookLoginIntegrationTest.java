package com.otognan.driverpete.security;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class FacebookLoginIntegrationTest extends BaseStatelesSecurityITTest {

    @Test
    public void getAnonymousUser() throws Exception {
        ResponseEntity<User> response = template.getForEntity(
                this.basePath + "api/user/current", User.class);
        User user = response.getBody();
        assertNull(user.getUsername());
    }
    
    @Test
    public void getSecuredAnonymously() throws Exception {
        ResponseEntity<String> response = template.getForEntity(
                this.basePath + "api/restricted/generic", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }
   
    @Test
    public void loginFlow() throws Exception {
        String token = this.getTestToken();
        User user = this.requestWithToken(token,
             this.basePath + "api/user/current", User.class).getBody();
        assertThat(user.getUsername(), equalTo("TestMike"));
    }

    @Test
    public void getSecuredAsUser() throws Exception {
        String token = this.getTestToken();
        String response = this.requestWithToken(token,
                this.basePath + "api/restricted/generic", String.class).getBody();
        assertThat(response, equalTo("AUTHENTICATED_ONLY"));
    }
    
    @Test
    public void getSecuredAsUserBadToken() throws Exception {
        String token = this.getTestToken() + 'x';
        ResponseEntity<String> response = this.requestWithToken(token,
                this.basePath + "api/restricted/generic", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }
}
