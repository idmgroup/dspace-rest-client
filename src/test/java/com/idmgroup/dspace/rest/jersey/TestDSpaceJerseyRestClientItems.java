package com.idmgroup.dspace.rest.jersey;

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

import java.io.InputStream;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;

import com.idmgroup.dspace.rest.TestUtils;

public class TestDSpaceJerseyRestClientItems {

    private void clean() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAsUser(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));
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

    private Bitstream createBitstream(DSpaceJerseyRestClient client, int itemId, String resourceName) {
        final String baseName = resourceName.replaceAll("^.*/([^/]+)$", "$1");
        InputStream content = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        Bitstream bitstream = client.items().item_idBitstreams(itemId)
                .post(content, baseName, TEST_UNICODE, null, 2015, 2, 17, null, null, null, Bitstream.class);
        assertEquals("created bitstream name", baseName, bitstream.getName());
        assertEquals("created bitstream bundle", "ORIGINAL", bitstream.getBundleName());
        if (baseName.endsWith(".png")) {
            assertEquals("created bitstream MIME type", "image/png", bitstream.getMimeType());
            assertEquals("created bitstream format", "image/png", bitstream.getFormat());
        } else if (baseName.endsWith(".txt")) {
            assertEquals("created bitstream MIME type", "text/plain", bitstream.getMimeType());
            assertEquals("created bitstream format", "Text", bitstream.getFormat());
        } else {
            // I just don't know
            assertEquals("created bitstream MIME type", "application/octet-stream", bitstream.getMimeType());
            assertEquals("created bitstream format", "application/octet-stream", bitstream.getFormat());
        }
        return bitstream;
    }

    private DSpaceJerseyRestClient newClient(String url) throws Exception {
        DSpaceJerseyRestClient client = new DSpaceJerseyRestClient(url);
        client.setBaseUrl(url);
        client.init();
        return client;
    }

    @Before
    public void setUp() throws Exception {
        TestUtils.trustAllSSL();
        clean();
    }

    @Test
    public void testCreateItemAndBitStreams() throws Exception {
        DSpaceJerseyRestClient client = newClient(DEMO_DSPACE_URL);
        client.loginJsonAsUser(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community result = client.communities().postJsonAsCommunity(community);
        final Integer comId = result.getId();

        Collection collection = new Collection();
        collection.setName(TEST_COLLECTION_NAME);
        Collection resultCol = client.communities().community_idCollections(comId)
                .postJsonAsCollection(collection, null, null, null);
        final Integer colId = resultCol.getId();

        Item item = new Item();
        item.setName("Logo IDM");
        Item resultItem = client.collections().collection_idItems(colId).postJsonAsItem(item);
        assertNotNull("created item", resultItem);
        assertNotNull("created item ID", resultItem.getId());
        assertTrue("created item ID > 0", resultItem.getId() > 0);
        assertThat("created item handle", resultItem.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer itemId = resultItem.getId();

        resultItem = client.items().item_id(itemId).getAsItemJson();
        assertEquals("get item ID", itemId, resultItem.getId());
        // XXX Well, I think I spotted a bug in DSpace REST API.
        assertEquals("get item name", /* FIXME "Logo IDM" */null, resultItem.getName());

        Bitstream bitstream;
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_big_transparent_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_small_transparent_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_big_vertical_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_small_vertical_hd.png");
        final Integer bsId = bitstream.getId();
        bitstream = client.bitstreams().bitstream_id(bsId).getAsBitstreamJson();
        assertEquals("get bitstream ID", bsId, bitstream.getId());
        assertEquals("get bitstream name", "logo-idm_small_vertical_hd.png", bitstream.getName());
        assertEquals("get bitstream description", TEST_UNICODE, bitstream.getDescription());

        bitstream = createBitstream(client, itemId, "com/idmgroup/text/ISO-8859-15.txt");
        final Integer isoId = bitstream.getId();
        bitstream = createBitstream(client, itemId, "com/idmgroup/text/UTF-8.txt");
        final Integer utfId = bitstream.getId();

        byte[] iso = client.bitstreams().bitstream_idRetrieve(isoId).getAs(byte[].class);
        // "Du caf. dans une cafeti.re!" plus LF
        assertEquals("ISO-8859-15 length", 27 + 1, iso.length);
        byte[] utf = client.bitstreams().bitstream_idRetrieve(utfId).getAs(byte[].class);
        // 5 chinese characters plus LF
        assertEquals("UTF-8 length", 5 * 3 + 1, utf.length);

        client.bitstreams().bitstream_id(bsId).deleteAs(String.class);
        try {
            bitstream = client.bitstreams().bitstream_id(bsId).getAsBitstreamJson();
            fail("Expected WebApplicationException to be thrown");
        } catch (WebApplicationException e) {
            assertEquals("HTTP status", 404, e.getResponse().getStatus());
        }
        // The other bitstreams will be deleted with the item.
        client.collections().collection_idItemsItem_id(colId, itemId).deleteAs(String.class);
        client.collections().collection_id(colId).deleteAs(String.class);
        client.communities().community_id(comId).deleteAs(String.class);
    }

}
