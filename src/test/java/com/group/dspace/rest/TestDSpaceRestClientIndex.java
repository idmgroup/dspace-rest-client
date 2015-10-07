package com.group.dspace.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.idmgroup.dspace.rest.DSpaceRestClient;

/**
 * Tests the REST client (Index).
 * 
 * @author arnaud
 */
public class TestDSpaceRestClientIndex {

    private static final String DEMO_DSPACE_URL = "https://demo.dspace.org/rest";

    private DSpaceRestClient newClient(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return new DSpaceRestClient(url, restTemplate);
    }

    @Before
    public void setUp() {
        TestUtils.trustAllSSL();
    }

    /**
     * Tests the HTML returned by the root URL.
     */
    @Test
    public void testIndex() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        String index = client.index();
        assertTrue("index title", index.indexOf("<title>DSpace REST - index</title>") >= 0);
        assertTrue("index index heading", index.indexOf("<h2>Index</h2>") >= 0);
        assertTrue("index communities heading", index.indexOf("<h2>Communities</h2>") >= 0);
        assertTrue("index collections heading", index.indexOf("<h2>Collections</h2>") >= 0);
        assertTrue("index items heading", index.indexOf("<h2>Items</h2>") >= 0);
        assertTrue("index bitstreams heading", index.indexOf("<h2>Bitstreams</h2>") >= 0);
    }

    /**
     * Tests the login.
     */
    @Test
    public void testLogin() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        String token = client.login("dspacedemo+admin@gmail.com", "dspace");
        assertTrue("dspace token", StringUtils.isNotBlank(token));
        assertTrue("dspace token format", token.matches("[-0-9A-Fa-f]+"));
    }

    /**
     * Tests the login with invalid credentials.
     */
    @Test
    public void testLoginFail() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        try {
            client.login("dspacedemo+admin@gmail.com", "DSPACE");
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException e) {
            assertEquals("HTTP status", HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    /**
     * Tests the logout.
     */
    @Test
    public void testLogout() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        client.login("dspacedemo+admin@gmail.com", "dspace");
        client.logout();
    }

    /**
     * Tests the logout with no token.
     */
    @Test
    public void testLogoutFail() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        try {
            client.logout();
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException e) {
            assertEquals("HTTP status", HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    /**
     * Tests the test URL.
     */
    @Test
    public void testTest() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        String testString = client.test();
        assertEquals("test string", "REST api is running.", testString);
    }

}
