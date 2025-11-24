package com.markovic.javazadanie.fx;

public class SessionManager {

    private static SessionManager instance;
    private String token;
    private String userEmail;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void clear() {
        this.token = null;
        this.userEmail = null;
    }
}
