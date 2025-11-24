package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.UserDto;

/**
 * Jednoduchý singleton na držanie info o prihlásenom userovi a JWT tokene.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String token;
    private UserDto currentUser;
    private String userEmail;
    private Long userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserEmail() {
        return userEmail;
    }


    public UserDto getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserDto currentUser) {
        this.currentUser = currentUser;
    }

    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    public void setUserId(Long id) {
        this.userId = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void clear() {
        this.token = null;
        this.currentUser = null;
    }
}
