package com.idmgroup.dspace.rest.jersey;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.idmgroup.dspace.rest.jersey.DemoDspaceOrg_Rest.Bitstreams;
import com.idmgroup.dspace.rest.jersey.DemoDspaceOrg_Rest.Collections;
import com.idmgroup.dspace.rest.jersey.DemoDspaceOrg_Rest.Communities;
import com.idmgroup.dspace.rest.jersey.DemoDspaceOrg_Rest.Items;
import com.idmgroup.dspace.rest.jersey.DemoDspaceOrg_Rest.Root;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * DSpace REST client implementation.
 * 
 * It is based on the wald2java tool and {@link Client} and manages the "rest-dspace-token" header for you.
 * 
 * @author arnaud
 */
public class DSpaceJerseyRestClient {

    private URI baseUri;

    private String baseUrl;

    private Client client;

    private String dspaceToken;

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * New instance, unconfigured.
     */
    public DSpaceJerseyRestClient() {
    }

    /**
     * New instance with a base URL and a configured {@link Client} instance.
     * 
     * @param baseUrl
     *            the base URL of the DSpace REST API, e.g. "https://demo.dspace.org/rest".
     * @param client
     */
    public DSpaceJerseyRestClient(String baseUrl, Client client) {
        setBaseUrl(baseUrl);
        setClient(client);
    }

    /**
     * Get the bitstreams resource.
     * 
     * @return the bitstreams resource.
     */
    public Bitstreams bitstreams() {
        return DemoDspaceOrg_Rest.bitstreams(client, baseUri);
    }

    /**
     * Get the collections resource.
     * 
     * @return the collections resource.
     */
    public Collections collections() {
        return DemoDspaceOrg_Rest.collections(client, baseUri);
    }

    /**
     * Get the communities resource.
     * 
     * @return the communities resource.
     */
    public Communities communities() {
        return DemoDspaceOrg_Rest.communities(client, baseUri);
    }

    /**
     * Gets baseUrl as an {@link URI} object.
     * 
     * @return the base URI.
     */
    public URI getBaseUri() {
        return baseUri;
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
     * Gets the jersey client.
     * 
     * @return the jersey client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Builds the client and add the filter to manage the "rest-dspace-token" header.
     * 
     * @throws URISyntaxException
     */
    @PostConstruct
    public void init() throws URISyntaxException {
        if (this.client == null) {
            ClientConfig cc = new DefaultClientConfig();
            this.client = Client.create(cc);
        }
        this.client.addFilter(new ClientFilter() {

            @Override
            public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
                if (dspaceToken != null) {
                    MultivaluedMap<String, Object> headers = request.getHeaders();
                    headers.add("rest-dspace-token", dspaceToken);
                }

                ClientResponse response = getNext().handle(request);

                return response;
            }

        });
        this.baseUri = new URI(baseUrl);
    }

    /**
     * Get the items resource.
     * 
     * @return the items resource.
     */
    public Items items() {
        return DemoDspaceOrg_Rest.items(client, baseUri);
    }

    public String loginJsonAs(User input) {
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
        String result = this.root().login().postJsonAs(input, String.class);
        if (result == null || result.length() <= 0)
            result = null;
        dspaceToken = result;

        return result;
    }

    public String loginXmlAs(User input) {
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
        String result = this.root().login().postXmlAs(input, String.class);
        if (result == null || result.length() <= 0)
            result = null;
        dspaceToken = result;

        return result;
    }

    public void logout() {
        try {
            this.root().logout().postAs(String.class);
        } finally {
            dspaceToken = null;
        }
    }

    /**
     * Get the root resource.
     * 
     * @return the root resource.
     */
    public Root root() {
        return DemoDspaceOrg_Rest.root(client, baseUri);
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
     * Sets the jersey client.
     * 
     * @param client
     *            the jersey client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

}
