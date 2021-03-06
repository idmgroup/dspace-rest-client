package com.idmgroup.dspace.rest;

import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_ADMIN;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_PASSWORD;
import static com.idmgroup.dspace.rest.TestConstants.DEMO_DSPACE_URL;
import static com.idmgroup.dspace.rest.TestConstants.TEST_COLLECTION_NAME;
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

import com.idmgroup.dspace.rest.jersey.Collection;
import com.idmgroup.dspace.rest.jersey.Community;

public class TestDSpaceRestClientCollections {

    private DSpaceRestClient newClient(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return new DSpaceRestClient(url, restTemplate);
    }

    @Before
    public void setUp() {
        TestUtils.trustAllSSL();
    }

    @Test
    public void testCreateUpdateDeleteCollection() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        client.login(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community resultCom = client.createCommunity(community);
        final Integer comId = resultCom.getId();

        Collection collection = new Collection();
        collection.setName(TEST_COLLECTION_NAME);
        Collection resultCol = client.addCommunityCollection(comId, collection);
        assertNotNull("created collection", resultCol);
        assertNotNull("created collection ID", resultCol.getId());
        assertTrue("created collection ID > 0", resultCol.getId() > 0);
        assertThat("created collection handle", resultCol.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer colId = resultCol.getId();

        resultCol = client.getCollection(colId, null, 0, 0);
        assertEquals("get collection ID", colId, resultCol.getId());
        assertEquals("get collection name", TEST_COLLECTION_NAME, resultCol.getName());

        resultCol.setCopyrightText("Copyright for pictures with unicode " + TEST_UNICODE);
        resultCol.setIntroductoryText("An introductory text for pictures with unicode " + TEST_UNICODE);
        resultCol.setShortDescription("A short description for Arno.db pictures with unicode " + TEST_UNICODE);
        resultCol.setSidebarText("Sidebar text for pictures with unicode " + TEST_UNICODE);
        client.updateCollection(colId, resultCol);

        resultCol = client.getCollection(colId, null, 0, 0);
        assertEquals("get2 collection ID", colId, resultCol.getId());
        assertEquals("get2 collection name", TEST_COLLECTION_NAME, resultCol.getName());
        assertEquals("get2 collection copyright", "Copyright for pictures with unicode " + TEST_UNICODE,
                resultCol.getCopyrightText());
        assertEquals("get2 collection introduction", "An introductory text for pictures with unicode " + TEST_UNICODE,
                resultCol.getIntroductoryText());
        assertEquals("get2 collection description", "A short description for Arno.db pictures with unicode "
                + TEST_UNICODE, resultCol.getShortDescription());
        assertEquals("get2 collection sidebar", "Sidebar text for pictures with unicode " + TEST_UNICODE,
                resultCol.getSidebarText());

        client.deleteCollection(colId);
        try {
            resultCol = client.getCollection(colId, null, 0, 0);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException e) {
            assertEquals("HTTP status", HttpStatus.NOT_FOUND, e.getStatusCode());
        }
        client.deleteCommunity(comId);
    }

}
