package com.booking.dto;

import com.booking.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationRequest {

    @JsonAlias({"resource_id", "resourceId"})
    private Long resourceId;

    @JsonProperty("resource")
    private ResourceWrapper resource;

    @JsonAlias({"user_id", "userId"})
    private Long userId;

    @JsonProperty("user")
    private UserWrapper user;

    @NotNull(message = "Start time is required")
    @JsonAlias({"start_time", "startTime"})
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonAlias({"end_time", "endTime"})
    private LocalDateTime endTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be a non-negative decimal value")
    private BigDecimal price;

    private ReservationStatus status;

    public ReservationRequest() {}

    public ReservationRequest(Long resourceId, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, ReservationStatus status) {
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public Long getResourceId() {
        if (resourceId != null) return resourceId;
        if (resource != null) return resource.getId();
        return null;
    }

    public Long getUserId() {
        if (userId != null) return userId;
        if (user != null) return user.getId();
        return null;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public static class ResourceWrapper {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class UserWrapper {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}
