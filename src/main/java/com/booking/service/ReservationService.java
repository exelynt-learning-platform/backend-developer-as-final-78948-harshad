package com.booking.service;

import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.dto.StatusUpdateRequest;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ReservationSpecification;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        UserDetailsImpl currentUser = getCurrentAuthenticatedUser();

        // If user is USER role, restrict search strictly to their own user ID
        Long filterUserId = null;
        if (!isAdmin(currentUser)) {
            filterUserId = currentUser.getId();
        }

        Specification<Reservation> spec = ReservationSpecification.filterReservations(
                filterUserId, status, minPrice, maxPrice);

        return reservationRepository.findAll(spec, pageable)
                .map(ReservationResponse::new);
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        UserDetailsImpl currentUser = getCurrentAuthenticatedUser();

        // Check ownership if currentUser is not ADMIN
        if (!isAdmin(currentUser) && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this reservation");
        }

        return new ReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        // Enforce USER identity strictly from JWT context
        UserDetailsImpl currentUserDetails = getCurrentAuthenticatedUser();
        User user;
        if (request.getUserId() != null && isAdmin(currentUserDetails)) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else {
            user = userRepository.findById(currentUserDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        }

        // Validate Resource existence
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Selected resource is currently not available for booking");
        }

        // Validate start and end times
        if (request.getEndTime() == null || request.getStartTime() == null ||
                !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("Reservation end time must be strictly after start time");
        }

        ReservationStatus initialStatus = request.getStatus() != null ? request.getStatus() : ReservationStatus.PENDING;

        Reservation reservation = new Reservation(
                resource,
                user,
                request.getStartTime(),
                request.getEndTime(),
                initialStatus,
                request.getPrice()
        );

        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(saved);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long id, StatusUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        UserDetailsImpl currentUser = getCurrentAuthenticatedUser();

        // Check ownership if currentUser is not ADMIN
        if (!isAdmin(currentUser) && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this reservation");
        }

        reservation.setStatus(request.getStatus());
        Reservation updated = reservationRepository.save(reservation);
        return new ReservationResponse(updated);
    }

    @Transactional
    public void cancelOrDeleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        UserDetailsImpl currentUser = getCurrentAuthenticatedUser();

        // Check ownership if currentUser is not ADMIN
        if (!isAdmin(currentUser) && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel/delete this reservation");
        }

        // If USER cancels, set status to CANCELLED; if ADMIN, delete or cancel
        if (isAdmin(currentUser)) {
            reservationRepository.delete(reservation);
        } else {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails.getRole().contains("ADMIN") ||
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }

    private UserDetailsImpl getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        throw new AccessDeniedException("User is not authenticated");
    }
}
