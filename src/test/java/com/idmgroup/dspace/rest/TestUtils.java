package com.idmgroup.dspace.rest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.idmgroup.dspace.rest.jersey.Community;

/**
 * Test helpers.
 * 
 * @author arnaud
 */
public class TestUtils {

    /**
     * Cleanup code.
     * 
     * @param client
     * @param communityName
     */
    public static void cleanCommunitiesByName(DSpaceRestClient client, String communityName) {
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

    /**
     * The DSpace demo website has a self signed certificate... ignore.
     * 
     * Note: I tried generating a test keystore, that worked in Travis CI but that didn't work in eclipse, so I
     * reinstated this trustAllSSL thing for tests.
     */
    public static void trustAllSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        } };

        try {
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

}
