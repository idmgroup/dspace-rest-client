package com.idmgroup.dspace.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dspace.rest.common.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

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
     * Gets the REST template.
     * 
     * @return the REST template.
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * REST API: "/".
     * 
     * @return the HTML content of the REST index.
     */
    public String index() {
        return restTemplate.getForObject(baseUrl, String.class);
    }

    /**
     * REST API: "/login".
     * 
     * @param email
     *            the email to use.
     * @param password
     *            the password to use.
     * @return the authentication token.
     */
    public String login(String email, String password) {
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
        User user = new User(email, password);
        String token = restTemplate.postForObject(baseUrl + "/login", user, String.class);
        if (token == null || token.length() <= 0)
            token = null;
        return dspaceToken = token;
    }

    /**
     * REST API: "/logout".
     */
    public void logout() {
        try {
            restTemplate.postForObject(baseUrl + "/logout", toEntity(null, MediaType.APPLICATION_JSON), String.class);
        } finally {
            dspaceToken = null;
        }
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
     * Sets the REST template.
     * 
     * @param restTemplate
     *            the REST template.
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * REST API: "/test".
     * 
     * @return the test string "REST api is running.".
     */
    public String test() {
        return restTemplate.getForObject(baseUrl + "/test", String.class);
    }

    private HttpEntity<?> toEntity(Object request, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        if (dspaceToken != null) {
            headers.add("rest-dspace-token", dspaceToken);
        }
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }
        HttpEntity<Object> entity = new HttpEntity<Object>(request, headers);
        return entity;
    }

}
