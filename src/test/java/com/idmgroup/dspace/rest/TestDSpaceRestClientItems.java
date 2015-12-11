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

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.idmgroup.dspace.rest.jersey.Bitstream;
import com.idmgroup.dspace.rest.jersey.Collection;
import com.idmgroup.dspace.rest.jersey.Community;
import com.idmgroup.dspace.rest.jersey.Item;
import com.idmgroup.dspace.rest.jersey.MetadataEntry;

public class TestDSpaceRestClientItems {

    private Bitstream createBitstream(DSpaceRestClient client, int itemId, String resourceName) {
        final String baseName = resourceName.replaceAll("^.*/([^/]+)$", "$1");
        InputStream content = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        Bitstream bitstream = client.addItemBitstream(itemId, baseName, TEST_UNICODE, null, 2015, 2, 17, content);
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

    private DSpaceRestClient newClient(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return new DSpaceRestClient(url, restTemplate);
    }

    @Before
    public void setUp() {
        TestUtils.trustAllSSL();
    }

    @Test
    public void testCreateItemAndBitStreams() {
        DSpaceRestClient client = newClient(DEMO_DSPACE_URL);
        client.login(user(DEMO_DSPACE_ADMIN, DEMO_DSPACE_PASSWORD));

        Community community = new Community();
        community.setName(TEST_COMMUNITY_NAME);
        Community resultCom = client.createCommunity(community);
        final Integer comId = resultCom.getId();

        Collection collection = new Collection();
        collection.setName(TEST_COLLECTION_NAME);
        Collection resultCol = client.addCommunityCollection(comId, collection);
        final Integer colId = resultCol.getId();

        Item item = new Item();
        MetadataEntry titleMD = new MetadataEntry();
        titleMD.setKey("dc.title");
        titleMD.setValue("Logo IDM");
        item.getMetadata().add(titleMD);
        Item resultItem = client.addCollectionItem(colId, item);
        assertNotNull("created item", resultItem);
        assertNotNull("created item ID", resultItem.getId());
        assertTrue("created item ID > 0", resultItem.getId() > 0);
        assertThat("created item handle", resultItem.getHandle(), new Matches("[0-9]+/[0-9]+"));
        final Integer itemId = resultItem.getId();

        resultItem = client.getItem(itemId, null);
        assertEquals("get item ID", itemId, resultItem.getId());
        // XXX Well, I think I spotted a bug in DSpace REST API.
        assertEquals("get item name", "Logo IDM", resultItem.getName());

        Bitstream bitstream;
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_big_transparent_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_small_transparent_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_big_vertical_hd.png");
        bitstream = createBitstream(client, itemId, "com/idmgroup/brand/logo-idm_small_vertical_hd.png");
        final Integer bsId = bitstream.getId();
        bitstream = client.getBitstream(bitstream.getId(), null);
        assertEquals("get bitstream ID", bsId, bitstream.getId());
        assertEquals("get bitstream name", "logo-idm_small_vertical_hd.png", bitstream.getName());
        assertEquals("get bitstream description", TEST_UNICODE, bitstream.getDescription());

        bitstream = createBitstream(client, itemId, "com/idmgroup/text/ISO-8859-15.txt");
        final Integer isoId = bitstream.getId();
        bitstream = createBitstream(client, itemId, "com/idmgroup/text/UTF-8.txt");
        final Integer utfId = bitstream.getId();

        byte[] iso = client.getBitstreamData(isoId);
        // "Du caf. dans une cafeti.re!" plus LF
        assertEquals("ISO-8859-15 length", 27 + 1, iso.length);
        byte[] utf = client.getBitstreamData(utfId);
        // 5 chinese characters plus LF
        assertEquals("UTF-8 length", 5 * 3 + 1, utf.length);

        client.deleteBitstream(bsId);
        try {
            bitstream = client.getBitstream(bsId, null);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException e) {
            assertEquals("HTTP status", HttpStatus.NOT_FOUND, e.getStatusCode());
        }
        // The other bitstreams will be deleted with the item.
        client.deleteItem(itemId);
        client.deleteCollection(colId);
        client.deleteCommunity(comId);
    }
}
