package com.idmgroup.dspace.rest;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.idmgroup.dspace.rest.jersey.Community;

/**
 * Tests the REST client (Handle).
 * 
 * @author arnaud
 */
public class TestDSpaceRestClientHandle {

    private DSpaceRestClient newClient(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return new DSpaceRestClient(url, restTemplate);
    }

    @Before
    public void setUp() {
        TestUtils.trustAllSSL();
    }

    @Test
    @Ignore
    // Unignore when the context issue is fixed upstream.
    public void testGetCommunity() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        {
            Object sampleCommunity = client.getObject("123456789", "1", null, Object.class);
            assertEquals("sample community class Object", LinkedHashMap.class, sampleCommunity.getClass());
        }
        {
            Community sampleCommunity = client.getObject("123456789", "1", null, Community.class);
            assertEquals("sample community class Community", Community.class, sampleCommunity.getClass());
        }
    }

}
