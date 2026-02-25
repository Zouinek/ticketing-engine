package com.ticketmaster.booking.controller;

import com.ticketmaster.booking.dto.request.BookingRequest;
import com.ticketmaster.booking.dto.request.ConfirmBookingRequest;
import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.service.BookingService;
import com.ticketmaster.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for managing ticket bookings with optimistic locking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking",
               description = "Reserve tickets for an event. Tickets are held for 10 minutes.")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Creating booking: {}", request);
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking after payment",
               description = "Confirm booking and finalize ticket reservation")
    public ResponseEntity<Booking> confirmBooking(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmBookingRequest request) {
        log.info("Confirming booking: {}", id);
        Booking booking = bookingService.confirmBooking(id, request.getPaymentId());
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking",
               description = "Cancel booking and release tickets back to inventory")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "User requested cancellation") String reason) {
        log.info("Cancelling booking: {}", id);
        Booking booking = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking: {}", id);
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable Long userId) {
        log.info("Fetching bookings for user: {}", userId);
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all bookings for an event")
    public ResponseEntity<List<Booking>> getEventBookings(@PathVariable Long eventId) {
        log.info("Fetching bookings for event: {}", eventId);
        List<Booking> bookings = bookingService.getBookingsByEventId(eventId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable BookingStatus status) {
        log.info("Fetching bookings with status: {}", status);
        List<Booking> bookings = bookingService.getBookingByBookingStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    @Operation(summary = "Get all bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        log.info("Fetching all bookings");
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "booking-service");
        return ResponseEntity.ok(response);
    }
}
