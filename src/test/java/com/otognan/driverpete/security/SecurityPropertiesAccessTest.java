package com.otognan.driverpete.security;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.apache.log4j.Logger;


@Configuration
@PropertySource(value="classpath:security.properties",
    ignoreResourceNotFound=true)
class TestConfiguration {}


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes=TestConfiguration.class)
public class SecurityPropertiesAccessTest {
    
    @Autowired
    private ApplicationContext appContext;
    
    static Logger log = Logger.getLogger("TEST_LOGGER");

    @Test
    public void testSocialPropertyAccess() throws Exception {
        /*
         * Here we test that we have access to facebook security properties.
         * On travis these variables has to be set for the repo on the repository settings page
         * available only to the owner so its secure.
         * Locally we get these properties from security.properties file that has to be added to
         * src/main/resources
         */
        assertNotNull(appContext.getEnvironment().getProperty("facebook.appKey"));
        assertNotNull(appContext.getEnvironment().getProperty("facebook.appSecret"));
        assertNotNull(appContext.getEnvironment().getProperty("token.secret"));
    }
    
}
