package com.booking.dto;

import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.Role;
import com.booking.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DtoAndModelTest {

    @Test
    void testUserModel() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@booking.com");
        u.setPassword("pass");
        u.setFullName("Full Name");
        u.setRole(Role.ROLE_ADMIN);

        assertEquals(1L, u.getId());
        assertEquals("test@booking.com", u.getEmail());
        assertEquals("pass", u.getPassword());
        assertEquals("Full Name", u.getFullName());
        assertEquals(Role.ROLE_ADMIN, u.getRole());

        User u2 = new User(1L, "test@booking.com", "pass", "Full Name", Role.ROLE_ADMIN);
        assertEquals(u.hashCode(), u2.hashCode());
        assertEquals(u, u2);
    }

    @Test
    void testResourceModel() {
        Resource r = new Resource();
        r.setId(1L);
        r.setName("Room");
        r.setDescription("Desc");
        r.setType("ROOM");
        r.setLocation("Loc");
        r.setCapacity(10);
        r.setAvailable(true);

        assertEquals(1L, r.getId());
        assertEquals("Room", r.getName());
        assertEquals("Desc", r.getDescription());
        assertEquals("ROOM", r.getType());
        assertEquals("Loc", r.getLocation());
        assertEquals(10, r.getCapacity());
        assertTrue(r.getAvailable());

        Resource r2 = new Resource(1L, "Room", "Desc", "ROOM", "Loc", 10, true);
        assertEquals(r.hashCode(), r2.hashCode());
        assertEquals(r, r2);
    }

    @Test
    void testReservationModel() {
        User u = new User(2L, "u@b.com", "p", "Name", Role.ROLE_USER);
        Resource r = new Resource(3L, "Room", "D", "ROOM", "L", 5, true);

        Reservation res = new Reservation();
        res.setId(1L);
        res.setResource(r);
        res.setUser(u);
        res.setStartTime(LocalDateTime.now());
        res.setEndTime(LocalDateTime.now().plusHours(2));
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setPrice(new BigDecimal("99.99"));

        assertEquals(1L, res.getId());
        assertEquals(r, res.getResource());
        assertEquals(u, res.getUser());
        assertNotNull(res.getStartTime());
        assertNotNull(res.getEndTime());
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());
        assertEquals(new BigDecimal("99.99"), res.getPrice());

        Reservation res2 = new Reservation(r, u, res.getStartTime(), res.getEndTime(), ReservationStatus.CONFIRMED, new BigDecimal("99.99"));
        res2.setId(1L);
        assertEquals(res.hashCode(), res2.hashCode());
        assertEquals(res, res2);
    }

    @Test
    void testDtos() {
        AuthResponse ar = new AuthResponse("token", 1L, "e@b.com", "Name", "ROLE_USER");
        ar.setToken("t2");
        ar.setEmail("e2");
        ar.setFullName("f2");
        ar.setRole("ROLE_ADMIN");
        ar.setType("Bearer");
        assertEquals("t2", ar.getToken());
        assertEquals(1L, ar.getId());
        assertEquals("e2", ar.getEmail());
        assertEquals("f2", ar.getFullName());
        assertEquals("ROLE_ADMIN", ar.getRole());
        assertEquals("Bearer", ar.getType());

        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error");
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "Message", errors);
        er.setStatus(404);
        er.setError("Not Found");
        er.setMessage("Msg");
        er.setValidationErrors(errors);
        er.setTimestamp(LocalDateTime.now());
        assertEquals(404, er.getStatus());
        assertEquals("Not Found", er.getError());
        assertEquals("Msg", er.getMessage());
        assertEquals(errors, er.getValidationErrors());
        assertNotNull(er.getTimestamp());

        LoginRequest lr = new LoginRequest();
        lr.setEmail("email");
        lr.setPassword("pass");
        assertEquals("email", lr.getEmail());
        assertEquals("pass", lr.getPassword());

        RegisterRequest req = new RegisterRequest();
        req.setEmail("email");
        req.setPassword("pass");
        req.setFullName("full");
        req.setRole(Role.ROLE_USER);
        assertEquals("email", req.getEmail());
        assertEquals("pass", req.getPassword());
        assertEquals("full", req.getFullName());
        assertEquals(Role.ROLE_USER, req.getRole());

        StatusUpdateRequest sur = new StatusUpdateRequest();
        sur.setStatus(ReservationStatus.CANCELLED);
        assertEquals(ReservationStatus.CANCELLED, sur.getStatus());
    }

    @Test
    void testExceptions() {
        BadRequestException bre = new BadRequestException("bad request");
        assertEquals("bad request", bre.getMessage());

        ResourceNotFoundException rnfe = new ResourceNotFoundException("not found");
        assertEquals("not found", rnfe.getMessage());
    }
}
