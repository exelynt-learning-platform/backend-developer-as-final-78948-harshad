package com.booking.controller;

import com.booking.dto.LoginRequest;
import com.booking.dto.ReservationRequest;
import com.booking.dto.StatusUpdateRequest;
import com.booking.model.ReservationStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setupTokens() throws Exception {
        LoginRequest adminLogin = new LoginRequest("admin@booking.com", "Admin@123");
        MvcResult adminResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("token").asText();

        LoginRequest userLogin = new LoginRequest("user@booking.com", "User@123");
        MvcResult userResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        userToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    public void testCreateReservation_AsUser_Success() throws Exception {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(12).withMinute(0),
                new BigDecimal("120.75"),
                ReservationStatus.PENDING
        );

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userEmail", is("user@booking.com")))
                .andExpect(jsonPath("$.price", is(120.75)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    public void testCreateReservation_InvalidTimes_ReturnsBadRequest() throws Exception {
        // End time before start time
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(3).withHour(12).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                new BigDecimal("120.75"),
                ReservationStatus.PENDING
        );

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("end time must be strictly after start time")));
    }

    @Test
    public void testFilterReservationsByStatusAndPrice_AsAdmin() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("status", "CONFIRMED")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "200.00")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    public void testUpdateReservationStatus_AsUser_Success() throws Exception {
        StatusUpdateRequest updateRequest = new StatusUpdateRequest(ReservationStatus.CANCELLED);

        mockMvc.perform(put("/reservations/1/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    public void testGetReservations_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());
    }
}
