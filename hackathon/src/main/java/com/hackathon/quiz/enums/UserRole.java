package com.hackathon.quiz.enums;

/**
 * Application roles for RBAC. Aligns with Spring Security's "ROLE_" convention in authorities.
 */
public enum UserRole {
    USER,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + name();
    }
}
