package com.intellicase.domain;

/**
 * Domain object representing an authenticated system user.
 * Roles: PUBLIC_USER, FIELD_AGENT, CASE_SUPERVISOR, FBI_DIRECTOR,
 *        INTELLIGENCE_ANALYST, FORENSIC_SPECIALIST, SYSTEM_ADMIN
 */
public class AppUser {
    private final int id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;

    public AppUser(int id, String username, String fullName,
                   String email, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
