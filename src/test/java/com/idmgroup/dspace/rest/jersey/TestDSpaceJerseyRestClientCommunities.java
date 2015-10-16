package com.idmgroup.dspace.rest.jersey;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_ADMIN;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_PASSWORD;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static com.idmgroup.dspace.rest.TestConstants.TEST_COMMUNITY_NAME;
import static com.idmgroup.dspace.rest.jersey.JerseyTestUtils.user;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;

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
                    }
                }
            } else {
                break;
            }
            offset += 20;
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
        clean();
    }

    @After
    public void tearDown() throws Exception {
        clean();
    }

    @Test
    public void testCreateCommunity() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAs(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));
        try {
            Community community = new Community();
            community.setName(TEST_COMMUNITY_NAME);
            Community result = client.communities().postJsonAsCommunity(community);
            assertNotNull("created community", result);
            assertNotNull("created community ID", result.getId());
            assertTrue("created community ID > 0", result.getId() > 0);
            assertThat("created community handle", result.getHandle(), new Matches("[0-9]+/[0-9]+"));
        } finally {
            client.logout();
        }
    }

}
