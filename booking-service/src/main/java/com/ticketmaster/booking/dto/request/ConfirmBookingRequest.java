package com.ticketmaster.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingRequest {

    @NotBlank(message = "Payment ID is required")
    @Schema(description = "Payment transaction ID", example = "PAY-123456789")
    private String paymentId;
}
