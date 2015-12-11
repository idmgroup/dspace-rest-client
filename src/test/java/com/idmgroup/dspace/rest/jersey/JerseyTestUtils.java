package com.idmgroup.dspace.rest.jersey;

public class JerseyTestUtils {

    public static void cleanCommunitiesByName(DSpaceJerseyRestClient client, String communityName) {
        int offset = 0;
        while (true) {
            Community[] slice = client.communities().getAsJson(null, 20, offset, null, null, null, Community[].class);
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

    public static User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

}
