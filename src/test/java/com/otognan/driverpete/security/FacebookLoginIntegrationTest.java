package com.otognan.driverpete.security;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StatelessAuthentication.class })
@WebAppConfiguration
@IntegrationTest({})
public class FacebookLoginIntegrationTest {

    //private URL base;
    private RestTemplate template;
    private String basePath;

    @Before
    public void setUp() throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        // trust self signed certificate
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                builder.build(),
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory).build();

        this.template = new TestRestTemplate();
        this.template
                .setRequestFactory(new HttpComponentsClientHttpRequestFactory(
                        httpClient) {
                    @Override
                    protected HttpContext createHttpContext(
                            HttpMethod httpMethod, URI uri) {
                        HttpClientContext context = HttpClientContext.create();
                        RequestConfig.Builder builder = RequestConfig.custom()
                                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                                .setAuthenticationEnabled(false)
                                .setRedirectsEnabled(false)
                                .setConnectTimeout(1000)
                                .setConnectionRequestTimeout(1000)
                                .setSocketTimeout(1000);
                        context.setRequestConfig(builder.build());
                        return context;
                    }
                });
        
        this.basePath = "https://localhost:8443/";
    }

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
    
    private <T> ResponseEntity<T> requestWithToken(String token, String path, Class<T> returnType) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-AUTH-TOKEN", token);
        HttpEntity<T> requestEntity = new HttpEntity<T>(null, requestHeaders);
        return template.exchange(path,
              HttpMethod.GET, requestEntity, returnType);
    }

    private String getTokenWithFacebook(String facebookUsername, String facebookPassword) throws Exception {
        ResponseEntity<String> response = template.getForEntity(
                this.basePath + "auth/facebook", String.class);
        assertTrue(response.getStatusCode().is3xxRedirection());
        URI loginRedirect = response.getHeaders().getLocation();
        assertThat(loginRedirect.toString(), startsWith("https://www.facebook.com/v1.0/dialog/oauth"));
        
        // Perform facebook login automation with HTMLUnit
        WebClient webClient = new WebClient();
        // Disable SSL - otherwise redirect from facebook to our app will fail
        // because of testing certificates
        webClient.getOptions().setUseInsecureSSL(true);
        
        HtmlPage page1 = webClient.getPage(loginRedirect.toString());
        HtmlForm form = (HtmlForm) page1.getElementById("login_form");
        if (form == null) {
            throw new Exception("Login form is not found. Possibly we are not on login page. " +
                    "Most of the time it happens when facebook rejects our app url");
        }
        HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Log In").get(0);
        HtmlTextInput textField = form.getInputByName("email");
        textField.setValueAttribute(facebookUsername);
        HtmlPasswordInput textField2 = form.getInputByName("pass");
        textField2.setValueAttribute(facebookPassword);

        HtmlPage homePage = button.click();

        // Check that we are redirected back to the application
        assertThat(homePage.getUrl().toString(), startsWith(this.basePath));
        Cookie tokenCookie = webClient.getCookieManager().getCookie("AUTH-TOKEN");
        assertNotNull(tokenCookie);
        String token = tokenCookie.getValue();
        assertNotNull(token);
        return token;
    }
    
    private String getTestToken() throws Exception {
        return this.getTokenWithFacebook("testmike_tmnhopm_mcdonaldson@tfbnw.net", "1234");
    }
}
