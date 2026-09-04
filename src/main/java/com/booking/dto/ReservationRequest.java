package com.booking.dto;

import com.booking.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationRequest {

    @JsonProperty("resourceId")
    private Long resourceId;

    @JsonProperty("resource_id")
    private Long resourceIdSnake;

    @JsonProperty("resource")
    private ResourceWrapper resource;

    @NotNull(message = "Start time is required")
    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @JsonProperty("start_time")
    private LocalDateTime startTimeSnake;

    @NotNull(message = "End time is required")
    @JsonProperty("endTime")
    private LocalDateTime endTime;

    @JsonProperty("end_time")
    private LocalDateTime endTimeSnake;

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
        if (resourceIdSnake != null) return resourceIdSnake;
        if (resource != null) return resource.getId();
        return null;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime != null ? startTime : startTimeSnake;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime != null ? endTime : endTimeSnake;
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
}
