package com.booking.service;

import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.dto.StatusUpdateRequest;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User standardUser;
    private User adminUser;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        standardUser = new User(10L, "user@booking.com", "pass", "User", Role.ROLE_USER);
        adminUser = new User(20L, "admin@booking.com", "pass", "Admin", Role.ROLE_ADMIN);

        resource = new Resource(1L, "Room A", "Desc", "ROOM", "Loc", 10, true);

        reservation = new Reservation(
                resource,
                standardUser,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                ReservationStatus.PENDING,
                new BigDecimal("100.00")
        );
        reservation.setId(100L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(User user) {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetReservations_AsAdmin() {
        authenticateUser(adminUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(reservationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ReservationResponse> result = reservationService.getReservations(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetReservations_AsUser() {
        authenticateUser(standardUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(reservationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ReservationResponse> result = reservationService.getReservations(ReservationStatus.PENDING, new BigDecimal("50.00"), new BigDecimal("150.00"), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetReservationById_Success_Owner() {
        authenticateUser(standardUser);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetReservationById_AccessDenied() {
        User otherUser = new User(30L, "other@booking.com", "pass", "Other", Role.ROLE_USER);
        authenticateUser(otherUser);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class, () -> reservationService.getReservationById(100L));
    }

    @Test
    void testCreateReservation_Success() {
        authenticateUser(standardUser);
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"),
                ReservationStatus.PENDING
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(standardUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
    }

    @Test
    void testCreateReservation_ResourceUnavailable() {
        authenticateUser(standardUser);
        resource.setAvailable(false);
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"),
                ReservationStatus.PENDING
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(standardUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }

    @Test
    void testUpdateReservationStatus_Success() {
        authenticateUser(standardUser);
        StatusUpdateRequest updateRequest = new StatusUpdateRequest(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservationStatus(100L, updateRequest);

        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void testCancelOrDeleteReservation_AsAdmin_Deletes() {
        authenticateUser(adminUser);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).delete(reservation);

        assertDoesNotThrow(() -> reservationService.cancelOrDeleteReservation(100L));
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    void testCancelOrDeleteReservation_AsUser_Cancels() {
        authenticateUser(standardUser);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        assertDoesNotThrow(() -> reservationService.cancelOrDeleteReservation(100L));
        verify(reservationRepository, times(1)).save(reservation);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
}
