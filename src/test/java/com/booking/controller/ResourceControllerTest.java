package com.booking.controller;

import com.booking.dto.LoginRequest;
import com.booking.dto.ResourceRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setupTokens() throws Exception {
        // Get Admin Token
        LoginRequest adminLogin = new LoginRequest("admin@booking.com", "Admin@123");
        MvcResult adminResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode adminJson = objectMapper.readTree(adminResult.getResponse().getContentAsString());
        adminToken = adminJson.get("token").asText();

        // Get User Token
        LoginRequest userLogin = new LoginRequest("user@booking.com", "User@123");
        MvcResult userResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode userJson = objectMapper.readTree(userResult.getResponse().getContentAsString());
        userToken = userJson.get("token").asText();
    }

    @Test
    public void testGetAllResources_AsUser_ReturnsOk() throws Exception {
        mockMvc.perform(get("/resources")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    public void testCreateResource_AsAdmin_ReturnsCreated() throws Exception {
        ResourceRequest request = new ResourceRequest(
                "Test Room 101",
                "Small meeting space",
                "ROOM",
                "Building A",
                6,
                true
        );

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Room 101")))
                .andExpect(jsonPath("$.type", is("ROOM")));
    }

    @Test
    public void testCreateResource_AsUser_ReturnsForbidden() throws Exception {
        ResourceRequest request = new ResourceRequest(
                "Unauthorized Room",
                "Space",
                "ROOM",
                "Building B",
                10,
                true
        );

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteResource_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/resources/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
