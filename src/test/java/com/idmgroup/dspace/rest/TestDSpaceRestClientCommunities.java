package com.idmgroup.dspace.rest;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_ADMIN;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_PASSWORD;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static com.idmgroup.dspace.rest.TestConstants.TEST_COMMUNITY_NAME;
import static com.idmgroup.dspace.rest.TestConstants.TEST_UNICODE;
import static com.idmgroup.dspace.rest.jersey.JerseyTestUtils.user;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.idmgroup.dspace.rest.jersey.Community;

public class TestDSpaceRestClientCommunities {

    private void clean() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        client.login(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));
        cleanCommunitiesByName(client, TEST_COMMUNITY_NAME);
    }

    private void cleanCommunitiesByName(DSpaceRestClient client, String communityName) {
        int offset = 0;
        while (true) {
            Community[] slice = client.getCommunities(null, 20, offset);
            if (slice != null && slice.length > 0) {
                for (Community com : slice) {
                    if (communityName.equals(com.getName())) {
                        client.deleteCommunity(com.getId());
                    } else {
                        ++offset;
                    }
                }
            } else {
                break;
            }
        }
    }

    private DSpaceRestClient newClient(String url) {
        // Default ctor and setters for code coverage
        RestTemplate restTemplate = new RestTemplate();
        DSpaceRestClient client = new DSpaceRestClient();
        client.setBaseUrl(url);
        client.setRestTemplate(restTemplate);
        return client;
    }

    @Before
    public void setUp() {
        TestUtils.trustAllSSL();
        clean();
    }

    @Test
    public void testCreateUpdateDeleteCommunity() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        client.login(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community result = client.createCommunity(community);
        assertNotNull("created community", result);
        assertNotNull("created community ID", result.getId());
        assertTrue("created community ID > 0", result.getId() > 0);
        assertThat("created community handle", result.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer comId = result.getId();

        result = client.getCommunity(comId, null);
        assertEquals("get community ID", comId, result.getId());
        assertEquals("get community name", TEST_COMMUNITY_NAME, result.getName());

        result.setCopyrightText("Copyright with unicode " + TEST_UNICODE);
        result.setIntroductoryText("An introductory text with unicode " + TEST_UNICODE);
        result.setShortDescription("A short description for Arno.db with unicode " + TEST_UNICODE);
        result.setSidebarText("Sidebar text with unicode " + TEST_UNICODE);
        client.updateCommunity(comId, result);

        result = client.getCommunity(comId, null);
        assertEquals("get2 community ID", comId, result.getId());
        assertEquals("get2 community name", TEST_COMMUNITY_NAME, result.getName());
        assertEquals("get2 community copyright", "Copyright with unicode " + TEST_UNICODE, result.getCopyrightText());
        assertEquals("get2 community introduction", "An introductory text with unicode " + TEST_UNICODE,
                result.getIntroductoryText());
        assertEquals("get2 community description", "A short description for Arno.db with unicode " + TEST_UNICODE,
                result.getShortDescription());
        assertEquals("get2 community sidebar", "Sidebar text with unicode " + TEST_UNICODE, result.getSidebarText());

        client.deleteCommunity(comId);
        try {
            result = client.getCommunity(comId, null);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException e) {
            assertEquals("HTTP status", HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

}
