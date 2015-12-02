package com.idmgroup.dspace.rest.jersey;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.idmgroup.dspace.rest.TestUtils;

public class TestDSpaceJerseyRestClientHandle {

    private DSpaceJerseyRestClient newClient(String url) throws Exception {
        DSpaceJerseyRestClient client = new DSpaceJerseyRestClient(url);
        client.setBaseUrl(url);
        client.init();
        return client;
    }

    @Before
    public void setUp() throws Exception {
        TestUtils.trustAllSSL();
    }

    @Test
    @Ignore
    // Unignore when the context issue is fixed upstream.
    public void testGetCommunity() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        {
            Object sampleCommunity = client.handle().prefixSuffix("123456789", "1").getAsJson(Object.class);
            assertEquals("sample community class Object", LinkedHashMap.class, sampleCommunity.getClass());
        }
        {
            Community sampleCommunity = client.handle().prefixSuffix("123456789", "1").getAsJson(Community.class);
            assertEquals("sample community class Community", Community.class, sampleCommunity.getClass());
        }
    }

}
