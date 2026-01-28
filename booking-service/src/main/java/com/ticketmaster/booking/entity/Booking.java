package com.ticketmaster.booking.entity;


import com.ticketmaster.booking.util.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_booking_date", columnList = "bookingDate"),
        @Index(name = "idx_booking_status", columnList = "bookingStatus")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString

public class Booking {

    @Schema(description = "Unique Booking ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "ID of the Event being booked", example = "10")
    @NotNull(message = "Event ID is required")
    @Column(nullable = false)
    private Long eventId;

    @Schema(description = "ID of the User making the booking", example = "42")
    @NotNull(message = "User ID is required")
    @Column(nullable = false)
    @Positive(message = "User ID must be positive")
    private Long userId;

    @Schema(description = "Payment transaction ID from payment service", example = "PAY123456789")
    @Column(length = 100)
    private String paymentId;

    @Schema(description = "Number of tickets booked", example = "2")
    @NotNull(message = "Number of tickets is required")
    @Min(value = 1, message = "Must book at least 1 ticket")
    private Integer numberOfTickets;

    @Schema(description = "Status of the booking", example = "CONFIRMED")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Booking status is required")
    @Column(nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Schema(description = "Total price of the booking", example = "150.00")
    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private Double totalPrice;

    @Schema(description = "Date of the booking", example = "2024-06-15")
    @NotNull(message = "Booking date is required")
    private LocalDateTime bookingDate;

    @Schema(description = "Timestamp when the booking was created", example = "2024-06-10T14:30:00")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the booking was last updated", example = "2024-06-12T10:15:00")
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Schema(description = "Unique reference code for the booking", example = "ABC123XYZ")
    @Column(nullable = false, unique = true, length = 50)
    private String bookingReference;

    @Schema(description = "Reason for cancellation if the booking is cancelled", example = "User requested cancellation")
    @Column(length = 500)
    private String cancellationReason;

    @Schema(description = "Date when the booking was cancelled", example = "2024-06-14T09:00:00")
    private LocalDateTime cancellationDate;

    @Schema(description = "Date when the booking was confirmed", example = "2024-06-11T11:30:00")
    private LocalDateTime confirmationDate;

    @Schema(description = "Timestamp when pending booking will expire", example = "2024-06-10T14:40:00")
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING;
        }
        // Set expiration time for pending bookings (10 minutes from creation)
        if (bookingStatus == BookingStatus.PENDING) {
            expiresAt = LocalDateTime.now().plusMinutes(10);
        }
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
