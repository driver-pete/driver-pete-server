package com.otognan.driverpete;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

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
@ActiveProfiles("test")
public abstract class BaseStatelesSecurityITTest {
    
    //private URL base;
    protected RestTemplate template;
    protected String basePath;
    protected String testToken;
    
    @Before
    public void setUp() throws Exception {
        final HttpClient httpClient = UnsafeHttpsClient.createUnsafeClient();

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
        
        if (this.testToken == null) {
            this.testToken = getTokenWithFacebook("testmike_kyyttal_fergiewitz@tfbnw.net",
                    "QWERTYUIOP1234567890");
        }
    }
    
    protected String getTokenWithFacebook(String facebookUsername, String facebookPassword) throws Exception {
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
    
    protected String getTestToken() throws Exception {
        return this.testToken;
    }
    
    protected <T> ResponseEntity<T> requestWithToken(String token, String path, Class<T> returnType) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-AUTH-TOKEN", token);
        HttpEntity<T> requestEntity = new HttpEntity<T>(null, requestHeaders);
        return template.exchange(path,
              HttpMethod.GET, requestEntity, returnType);
    }
    
    protected <T> T serverAPI(final String token, Class<T> apiClass) {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-AUTH-TOKEN", token);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(this.basePath)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
                .setRequestInterceptor(requestInterceptor)
                .build();

       return restAdapter.create(apiClass);
    }
}
