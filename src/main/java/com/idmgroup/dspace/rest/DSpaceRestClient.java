package com.idmgroup.dspace.rest;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.idmgroup.dspace.rest.jersey.Bitstream;
import com.idmgroup.dspace.rest.jersey.Collection;
import com.idmgroup.dspace.rest.jersey.Community;
import com.idmgroup.dspace.rest.jersey.Item;
import com.idmgroup.dspace.rest.jersey.MetadataEntry;
import com.idmgroup.dspace.rest.jersey.ResourcePolicy;
import com.idmgroup.dspace.rest.jersey.Status;
import com.idmgroup.dspace.rest.jersey.User;

/**
 * DSpace REST client implementation.
 * 
 * It is based on the Spring {@link RestTemplate} and manages the "rest-dspace-token" header for you.
 * 
 * @author arnaud
 */
public class DSpaceRestClient {

    private String baseUrl;

    private String dspaceToken;

    private final Log logger = LogFactory.getLog(getClass());

    private MediaType requestMediaType = MediaType.APPLICATION_JSON;

    private RestTemplate restTemplate;

    /**
     * New instance, unconfigured.
     */
    public DSpaceRestClient() {
    }

    /**
     * New instance with a base URL and a configured {@link RestTemplate} instance.
     * 
     * @param baseUrl
     *            the base URL of the DSpace REST API, e.g. "https://demo.dspace.org/rest".
     * @param restTemplate
     */
    public DSpaceRestClient(String baseUrl, RestTemplate restTemplate) {
        setBaseUrl(baseUrl);
        setRestTemplate(restTemplate);
    }

    /**
     * Gets the base URL of the DSpace REST API.
     * 
     * @return the base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the request media type.
     * 
     * @return the request media type.
     */
    public MediaType getRequestMediaType() {
        return requestMediaType;
    }

    /**
     * Gets the REST template.
     * 
     * @return the REST template.
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Sets the base URL of the DSpace REST API.
     * 
     * @param baseUrl
     *            the base URL of the DSpace REST API, e.g. "https://demo.dspace.org/rest".
     */
    public void setBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/"))
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        else
            this.baseUrl = baseUrl;
    }

    /**
     * Sets the request media type.
     * 
     * @param requestMediaType
     *            the request media type.
     */
    public void setRequestMediaType(MediaType requestMediaType) {
        this.requestMediaType = requestMediaType;
    }

    /**
     * Sets the REST template.
     * 
     * @param restTemplate
     *            the REST template.
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpEntity<?> toEntity(Object requestBody) {
        HttpHeaders headers = new HttpHeaders();
        if (dspaceToken != null) {
            headers.add("rest-dspace-token", dspaceToken);
        }
        if (requestMediaType != null) {
            headers.setContentType(requestMediaType);
        }
        HttpEntity<Object> entity = new HttpEntity<Object>(requestBody, headers);
        return entity;
    }

    public byte[] getBitstreamData(Integer bitstream_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}/retrieve")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        byte[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), byte[].class).getBody();

        return result;
    }

    public Void addBitstreamPolicy(Integer bitstream_id, String userIP, String userAgent, String xforwardedfor, ResourcePolicy requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}/policy")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(requestBody), Void.class).getBody();

        return result;
    }

    public ResourcePolicy[] getBitstreamPolicies(Integer bitstream_id) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}/policy");
        ResourcePolicy[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), ResourcePolicy[].class).getBody();

        return result;
    }

    public Void updateBitstream(Integer bitstream_id, String userIP, String userAgent, String xforwardedfor, Bitstream requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.PUT,
                toEntity(requestBody), Void.class).getBody();

        return result;
    }

    public Void deleteBitstream(Integer bitstream_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Bitstream getBitstream(Integer bitstream_id, String expand, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}")
        .queryParam("expand", expand)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Bitstream result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Bitstream.class).getBody();

        return result;
    }

    public Void updateBitstreamData(Integer bitstream_id, String userIP, String userAgent, String xforwardedfor, InputStream requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}/data")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.PUT,
                toEntity(new InputStreamResource(requestBody)), Void.class).getBody();

        return result;
    }

    public Void deleteBitstreamPolicy(Integer policy_id, Integer bitstream_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("policy_id", policy_id);
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams/{bitstream_id}/policy/{policy_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Bitstream[] getBitstreams(String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/bitstreams")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Bitstream[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Bitstream[].class).getBody();

        return result;
    }

    public Void updateCommunity(Integer community_id, String userIP, String userAgent, String xforwardedfor, Community requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.PUT,
                toEntity(requestBody), Void.class).getBody();

        return result;
    }

    public Community getCommunity(Integer community_id, String expand, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}")
        .queryParam("expand", expand)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Community.class).getBody();

        return result;
    }

    public Void deleteCommunity(Integer community_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Community addCommunityCommunity(Integer community_id, String userIP, String userAgent, String xforwardedfor, Community requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/communities")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(requestBody), Community.class).getBody();

        return result;
    }

    public Community[] getCommunityCommunities(Integer community_id, String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/communities")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Community[].class).getBody();

        return result;
    }

    public Collection addCommunityCollection(Integer community_id, String userIP, String userAgent, String xforwardedfor, Collection requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/collections")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Collection result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(requestBody), Collection.class).getBody();

        return result;
    }

    public Collection[] getCommunityCollections(Integer community_id, String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/collections")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Collection[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Collection[].class).getBody();

        return result;
    }

    public Community[] getTopCommunities(String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/top-communities")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Community[].class).getBody();

        return result;
    }

    public Void deleteCommunityCollection(Integer collection_id, Integer community_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        uriVariables.put("community_id", community_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/collections/{collection_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Void deleteCommunityCommunity(Integer community_id, Integer community_id2, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("community_id", community_id);
        uriVariables.put("community_id2", community_id2);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities/{community_id}/communities/{community_id2}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Community[] getCommunities(String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Community[].class).getBody();

        return result;
    }

    public Community createCommunity(String userIP, String userAgent, String xforwardedfor, Community requestBody) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/communities")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Community result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.POST,
                toEntity(requestBody), Community.class).getBody();

        return result;
    }

    public Void updateCollection(Integer collection_id, String userIP, String userAgent, String xforwardedfor, Collection requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.PUT,
                toEntity(requestBody), Void.class).getBody();

        return result;
    }

    public Void deleteCollection(Integer collection_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Collection getCollection(Integer collection_id, String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Collection result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Collection.class).getBody();

        return result;
    }

    public Void deleteCollectionItem(Integer collection_id, Integer item_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}/items/{item_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Collection findCollectionByName() {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/find-collection");
        Collection result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.POST,
                toEntity(null), Collection.class).getBody();

        return result;
    }

    public Item addCollectionItem(Integer collection_id, String userIP, String userAgent, String xforwardedfor, Item requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}/items")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Item result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(requestBody), Item.class).getBody();

        return result;
    }

    public Item[] getCollectionItems(Integer collection_id, String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("collection_id", collection_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections/{collection_id}/items")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Item[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Item[].class).getBody();

        return result;
    }

    public Collection[] getCollections(String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/collections")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Collection[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Collection[].class).getBody();

        return result;
    }

    public Object getObject(String prefix, String suffix, String expand) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("prefix", prefix);
        uriVariables.put("suffix", suffix);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/handle/{prefix}/{suffix}")
        .queryParam("expand", expand);
        Object result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Object.class).getBody();

        return result;
    }

    public String test() {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/test");
        String result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), String.class).getBody();

        return result;
    }

    public Status status() {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/status");
        Status result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Status.class).getBody();

        return result;
    }

    public String login(User requestBody) {
        // Logout just in case.
        if (dspaceToken != null) {
            try {
                logout();
            } catch (Exception e) {
                logger.error(e);
            } finally {
                dspaceToken = null;
            }
        }
        // Login
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/login");
        String result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.POST,
                toEntity(requestBody), String.class).getBody();
        if (result == null || result.length() <= 0)
            result = null;
        dspaceToken = result;

        return result;
    }

    public Void logout() {
        try {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/logout");
            Void result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.POST,
                    toEntity(null), Void.class).getBody();

            return result;
        } finally {
            dspaceToken = null;
        }
    }

    public String sayHtmlHello() {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/");
        String result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), String.class).getBody();

        return result;
    }

    public Item getItem(Integer item_id, String expand, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}")
        .queryParam("expand", expand)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Item result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Item.class).getBody();

        return result;
    }

    public Void deleteItem(Integer item_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Void updateItemMetadata(Integer item_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/metadata")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.PUT,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Void addItemMetadata(Integer item_id, String userIP, String userAgent, String xforwardedfor, MetadataEntry requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/metadata")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(requestBody), Void.class).getBody();

        return result;
    }

    public Void deleteItemMetadata(Integer item_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/metadata")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public MetadataEntry getItemMetadata(Integer item_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/metadata")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        MetadataEntry result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), MetadataEntry.class).getBody();

        return result;
    }

    public Bitstream[] getItemBitstreams(Integer item_id, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/bitstreams")
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Bitstream[] result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.GET,
                toEntity(null), Bitstream[].class).getBody();

        return result;
    }

    public Bitstream addItemBitstream(Integer item_id, String name, String description, Integer groupId, Integer year, Integer month, Integer day, String userIP, String userAgent, String xforwardedfor, InputStream requestBody) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/bitstreams")
        .queryParam("name", name)
        .queryParam("description", description)
        .queryParam("groupId", groupId)
        .queryParam("year", year)
        .queryParam("month", month)
        .queryParam("day", day)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Bitstream result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.POST,
                toEntity(new InputStreamResource(requestBody)), Bitstream.class).getBody();

        return result;
    }

    public Item[] findItemsByMetadataField(String expand, String userIP, String userAgent, String xforwardedfor, MetadataEntry requestBody) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/find-by-metadata-field")
        .queryParam("expand", expand)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Item[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.POST,
                toEntity(requestBody), Item[].class).getBody();

        return result;
    }

    public Void deleteItemBitstream(Integer item_id, Integer bitstream_id, String userIP, String userAgent, String xforwardedfor) {

        Map<String, Object> uriVariables = new LinkedHashMap<String, Object>();
        uriVariables.put("item_id", item_id);
        uriVariables.put("bitstream_id", bitstream_id);
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items/{item_id}/bitstreams/{bitstream_id}")
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Void result = restTemplate.exchange(queryBuilder.buildAndExpand(uriVariables).toUri(), HttpMethod.DELETE,
                toEntity(null), Void.class).getBody();

        return result;
    }

    public Item[] getItems(String expand, Integer limit, Integer offset, String userIP, String userAgent, String xforwardedfor) {
        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(baseUrl).path("/items")
        .queryParam("expand", expand)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .queryParam("userIP", userIP)
        .queryParam("userAgent", userAgent)
        .queryParam("xforwardedfor", xforwardedfor);
        Item[] result = restTemplate.exchange(queryBuilder.build().toUri(), HttpMethod.GET,
                toEntity(null), Item[].class).getBody();

        return result;
    }


}
