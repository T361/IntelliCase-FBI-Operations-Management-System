package com.intellicase.presentation;

import com.intellicase.domain.AppUser;

/**
 * GoF Singleton tracking the currently authenticated user.
 * Cleared on logout; checked by controllers for role-based access.
 */
public final class SessionManager {
    private static SessionManager instance;
    private AppUser currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(AppUser user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public String getRole() {
        return currentUser != null ? currentUser.getRole() : "PUBLIC_USER";
    }

    public String getDisplayName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
}
