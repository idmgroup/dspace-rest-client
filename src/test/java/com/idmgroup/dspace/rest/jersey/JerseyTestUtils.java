package com.idmgroup.dspace.rest.jersey;

public class JerseyTestUtils {

    public static User user(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

}
