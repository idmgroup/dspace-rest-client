package com.idmgroup.dspace.rest.jersey;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_ADMIN;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_PASSWORD;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static com.idmgroup.dspace.rest.TestConstants.TEST_COLLECTION_NAME;
import static com.idmgroup.dspace.rest.TestConstants.TEST_COMMUNITY_NAME;
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

public class TestDSpaceJerseyRestClientCollections {

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
        TestUtils.trustAllSSL();
        clean();
    }

    @Test
    public void testCreateUpdateDeleteCollection() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAs(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community result = client.communities().postJsonAsCommunity(community);
        final Integer comId = result.getId();

        Collection collection = new Collection();
        collection.setName(TEST_COLLECTION_NAME);
        Collection resultCol = client.communities().community_idCollections(comId)
                .postJsonAsCollection(collection, null, null, null);
        assertNotNull("created collection", resultCol);
        assertNotNull("created collection ID", resultCol.getId());
        assertTrue("created collection ID > 0", resultCol.getId() > 0);
        assertThat("created collection handle", resultCol.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer colId = resultCol.getId();

        resultCol = client.collections().collection_id(colId).getAsCollectionJson(null, 0, 0, null, null, null);
        assertEquals("get collection ID", colId, resultCol.getId());
        assertEquals("get collection name", TEST_COLLECTION_NAME, resultCol.getName());

        resultCol.setShortDescription("A short description for Arno.db pictures");
        client.collections().collection_id(colId).putJsonAs(resultCol, String.class);

        resultCol = client.collections().collection_id(colId).getAsCollectionJson(null, 0, 0, null, null, null);
        assertEquals("get2 collection ID", colId, resultCol.getId());
        assertEquals("get2 collection name", TEST_COLLECTION_NAME, resultCol.getName());
        assertEquals("get2 collection description", "A short description for Arno.db pictures",
                resultCol.getShortDescription());

        client.collections().collection_id(colId).deleteAs(String.class);
        try {
            resultCol = client.collections().collection_id(colId).getAsCollectionJson(null, 0, 0, null, null, null);
            fail("Expected WebApplicationException to be thrown");
        } catch (WebApplicationException e) {
            assertEquals("HTTP status", 404, e.getResponse().getStatus());
        }
        client.communities().community_id(comId).deleteAs(String.class);
    }

}
