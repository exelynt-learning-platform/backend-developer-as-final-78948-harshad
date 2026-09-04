package com.booking.controller;

import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.dto.StatusUpdateRequest;
import com.booking.model.ReservationStatus;
import com.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservations", description = "Endpoints for creating and managing bookings")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Get reservations with optional filtering (status, minPrice, maxPrice) and pagination/sorting. ADMIN views all; USER views only their own.")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPriceCamel,
            @RequestParam(name = "min_price", required = false) BigDecimal minPriceSnake,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPriceCamel,
            @RequestParam(name = "max_price", required = false) BigDecimal maxPriceSnake,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortByCamel,
            @RequestParam(name = "sort_by", required = false) String sortBySnake,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDirCamel,
            @RequestParam(name = "sort_dir", required = false) String sortDirSnake) {

        BigDecimal effectiveMinPrice = minPriceCamel != null ? minPriceCamel : minPriceSnake;
        BigDecimal effectiveMaxPrice = maxPriceCamel != null ? maxPriceCamel : maxPriceSnake;
        String effectiveSortBy = sortBySnake != null ? sortBySnake : sortByCamel;
        String effectiveSortDir = sortDirSnake != null ? sortDirSnake : sortDirCamel;

        Sort sort = effectiveSortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(effectiveSortBy).ascending() : Sort.by(effectiveSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReservationResponse> reservations = reservationService.getReservations(status, effectiveMinPrice, effectiveMaxPrice, pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new reservation (User identity automatically taken from JWT)")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update reservation status (PENDING, CONFIRMED, CANCELLED)")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation status or details")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel/delete reservation")
    public ResponseEntity<Void> cancelOrDeleteReservation(@PathVariable Long id) {
        reservationService.cancelOrDeleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
