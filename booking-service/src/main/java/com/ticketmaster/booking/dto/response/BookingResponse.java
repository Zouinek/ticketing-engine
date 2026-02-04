package com.ticketmaster.booking.dto.response;

import com.ticketmaster.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    @Schema(description = "Unique Booking ID", example = "BK123456789")
    private String bookingId;

    @Schema(description = "Status of the booking", example = "PENDING")
    private BookingStatus status;

    @Schema(description = "Expiration date of the booking", example = "2026-01-28T15:30:00")
    private LocalDateTime expiredAt;

    @Schema(description = "URL for completing the payment", example = "https://paymentgateway.com/pay/BK123456789")
    private String paymentUrl;

    @Schema(description = "Total price for the booking", example = "99.99")
    private BigDecimal totalPrice;

    @Schema(description = "Payment transaction ID from payment service", example = "PAY123456789")
    private Long paymentId;

    @Schema(description = "ID of the User who made the booking", example = "42")
    private Long userId;

    @Schema(description = "ID of the Event being booked", example = "10")
    private Long eventId;

    @Schema(description = "ID of the Ticket associated with the booking", example = "1001")
    private Long ticketId;

    @Schema(description = "ID of the Seat being booked", example = "25")
    private Long seatId;

    @Schema(description = "When the booking was created", example = "2026-01-28T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Number of tickets booked", example = "2")
    private Integer quantity;


}
