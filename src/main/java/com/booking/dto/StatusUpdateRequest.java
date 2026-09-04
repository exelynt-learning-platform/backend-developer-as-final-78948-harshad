package com.booking.dto;

import com.booking.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReservationStatus status;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(ReservationStatus status) {
        this.status = status;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
