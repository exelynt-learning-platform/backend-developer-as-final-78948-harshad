package com.booking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("token")
    @JsonAlias({"access_token", "accessToken", "jwt", "jwt_token", "jwtToken"})
    private String token;

    private String type = "Bearer";

    @JsonProperty("id")
    @JsonAlias({"user_id", "userId"})
    private Long id;

    @JsonProperty("email")
    @JsonAlias({"user_email", "userEmail"})
    private String email;

    @JsonProperty("fullName")
    @JsonAlias({"full_name", "user_name", "userName", "name"})
    private String fullName;

    @JsonProperty("role")
    @JsonAlias({"user_role", "userRole", "authorities"})
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String email, String fullName, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
