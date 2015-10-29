package com.idmgroup.dspace.rest.jersey;

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

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;

import com.idmgroup.dspace.rest.TestUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Tests the REST client (Index).
 * 
 * @author arnaud
 */
public class TestDSpaceJerseyRestClientCommunities {

    private void clean() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAs(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));
        cleanCommunitiesByName(client, TEST_COMMUNITY_NAME);
    }

    private void cleanCommunitiesByName(DSpaceJerseyRestClient client, String communityName) {
        int offset = 0;
        while (true) {
            // FIXME apparently jersey has difficulties with Community entities in JSON => XML.
            Community[] slice = client.communities().getAsXml(null, 20, offset, null, null, null, Community[].class);
            if (slice != null && slice.length > 0) {
                for (Community com : slice) {
                    if (communityName.equals(com.getName())) {
                        client.communities().community_id(com.getId()).deleteAs(String.class);
                    } else {
                        ++offset;
                    }
                }
            } else {
                break;
            }
        }
    }

    private DSpaceJerseyRestClient newClient(String url) throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        Client cl = Client.create(cc);
        DSpaceJerseyRestClient client = new DSpaceJerseyRestClient(url, cl);
        client.init();
        return client;
    }

    @Before
    public void setUp() throws Exception {
        TestUtils.trustAllSSL();
        clean();
    }

    @Test
    public void testCreateUpdateDeleteCommunity() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAs(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community result = client.communities().postJsonAsCommunity(community);
        assertNotNull("created community", result);
        assertNotNull("created community ID", result.getId());
        assertTrue("created community ID > 0", result.getId() > 0);
        assertThat("created community handle", result.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer comId = result.getId();

        result = client.communities().community_id(comId).getAsCommunityJson();
        assertEquals("get community ID", comId, result.getId());
        assertEquals("get community name", TEST_COMMUNITY_NAME, result.getName());

        result.setCopyrightText("Copyright with unicode " + TEST_UNICODE);
        result.setIntroductoryText("An introductory text with unicode " + TEST_UNICODE);
        result.setShortDescription("A short description for Arno.db with unicode " + TEST_UNICODE);
        result.setSidebarText("Sidebar text with unicode " + TEST_UNICODE);
        client.communities().community_id(comId).putJsonAs(result, String.class);

        result = client.communities().community_id(comId).getAsCommunityJson();
        assertEquals("get2 community ID", comId, result.getId());
        assertEquals("get2 community name", TEST_COMMUNITY_NAME, result.getName());
        assertEquals("get2 community copyright", "Copyright with unicode " + TEST_UNICODE, result.getCopyrightText());
        assertEquals("get2 community introduction", "An introductory text with unicode " + TEST_UNICODE,
                result.getIntroductoryText());
        assertEquals("get2 community description", "A short description for Arno.db with unicode " + TEST_UNICODE,
                result.getShortDescription());
        assertEquals("get2 community sidebar", "Sidebar text with unicode " + TEST_UNICODE, result.getSidebarText());

        client.communities().community_id(comId).deleteAs(String.class);
        try {
            result = client.communities().community_id(comId).getAsCommunityJson();
            fail("Expected WebApplicationException to be thrown");
        } catch (WebApplicationException e) {
            assertEquals("HTTP status", 404, e.getResponse().getStatus());
        }
    }

}
