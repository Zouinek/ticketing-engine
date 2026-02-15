package com.ticketmaster.booking.service;

import com.ticketmaster.booking.dto.request.BookingRequest;
import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.exception.BookingAlreadyCancelledException;
import com.ticketmaster.booking.exception.BookingExpiredException;
import com.ticketmaster.booking.exception.BookingNotFoundException;
import com.ticketmaster.booking.exception.SeatNotAvailableException;
import com.ticketmaster.booking.grpc.EventGrpcClient;
import com.ticketmaster.booking.repository.BookingRepository;
import com.ticketmaster.common.enums.BookingStatus;
import com.ticketmaster.common.grpc.GetEventResponse;
import com.ticketmaster.common.grpc.ReserveTicketsResponse;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventGrpcClient eventGrpcClient;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    public List<Booking> getBookingByBookingStatus(BookingStatus status) {
        return bookingRepository.findByBookingStatus(status);
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEventId(eventId);
    }

    /**
     * Create a new booking with optimistic locking retry logic
     * This reserves tickets in the event-service
     */
    @Transactional
    public Booking createBooking(BookingRequest bookingRequest) {
        log.info("Creating booking for user: {}, event: {}, seats: {}",
                bookingRequest.getUserId(), bookingRequest.getEventId(), bookingRequest.getQuantity());

        // Step 1: Get event details to calculate price
        GetEventResponse event = getEventWithRetry(bookingRequest.getEventId());

        // Step 2: Calculate total price
        BigDecimal totalPrice = BigDecimal.valueOf(event.getTicketPrice())
                .multiply(BigDecimal.valueOf(bookingRequest.getQuantity()));

        // Step 3: Reserve tickets with retry logic for optimistic locking
        ReserveTicketsResponse reservationResponse = reserveTicketsWithRetry(
                bookingRequest.getEventId(),
                bookingRequest.getQuantity()
        );

        if (!reservationResponse.getSuccess()) {
            throw new SeatNotAvailableException(reservationResponse.getMessage());
        }

        // Step 4: Create booking entity
        Booking booking = Booking.builder()
                .userId(bookingRequest.getUserId())
                .eventId(bookingRequest.getEventId())
                .seatId(bookingRequest.getSeatId())
                .numberOfTickets(bookingRequest.getQuantity())
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .bookingReference(generateBookingReference())
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10 min to pay
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {}, expires at: {}",
                savedBooking.getBookingReference(), savedBooking.getExpiresAt());

        return savedBooking;
    }

    /**
     * Confirm booking after successful payment
     */
    @Transactional
    public Booking confirmBooking(Long bookingId, String paymentId) {
        log.info("Confirming booking: {}, paymentId: {}", bookingId, paymentId);

        Booking booking = getBookingById(bookingId);

        // Validate booking can be confirmed
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException("Booking is already cancelled");
        }

        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BookingExpiredException("Booking has expired");
        }

        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BookingExpiredException("Booking expiration time has passed");
        }

        // Update booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentId(paymentId);
        booking.setConfirmationDate(LocalDateTime.now());
        booking.setExpiresAt(null); // Clear expiration

        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed successfully: {}", confirmedBooking.getBookingReference());

        return confirmedBooking;
    }

    /**
     * Cancel a booking and release tickets back to event
     */
    @Transactional
    public Booking cancelBooking(Long bookingId, String reason) {
        log.info("Cancelling booking: {}, reason: {}", bookingId, reason);

        Booking booking = getBookingById(bookingId);

        // Validate booking can be cancelled
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException("Booking is already cancelled");
        }

        // Release tickets back to event
        try {
            eventGrpcClient.releaseTickets(booking.getEventId(), booking.getNumberOfTickets());
        } catch (StatusRuntimeException e) {
            log.error("Failed to release tickets for booking {}: {}", bookingId, e.getMessage());
            // Continue with cancellation even if release fails
        }

        // Update booking
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancellationDate(LocalDateTime.now());

        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully: {}", cancelledBooking.getBookingReference());

        return cancelledBooking;
    }

    /**
     * Release expired bookings (called by scheduler)
     */
    @Transactional
    public void releaseExpiredBookings() {
        log.info("Checking for expired bookings...");

        List<Booking> expiredBookings = bookingRepository
                .findByBookingStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

        if (expiredBookings.isEmpty()) {
            log.info("No expired bookings found");
            return;
        }

        log.info("Found {} expired bookings to release", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                // Release tickets back to event
                eventGrpcClient.releaseTickets(booking.getEventId(), booking.getNumberOfTickets());

                // Update booking status
                booking.setBookingStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);

                log.info("Released expired booking: {}", booking.getBookingReference());

            } catch (Exception e) {
                log.error("Failed to release expired booking {}: {}",
                        booking.getBookingReference(), e.getMessage());
            }
        }
    }

    /**
     * Get event details with retry logic
     */
    private GetEventResponse getEventWithRetry(Long eventId) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                return eventGrpcClient.getEvent(eventId);
            } catch (StatusRuntimeException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("Failed to get event after {} attempts: {}", attempts, e.getMessage());
                    throw e;
                }
                log.warn("Failed to get event, attempt {}/{}: {}", attempts, MAX_RETRY_ATTEMPTS, e.getMessage());
                sleep(RETRY_DELAY_MS * attempts);
            }
        }
        throw new RuntimeException("Failed to get event after retries");
    }

    /**
     * Reserve tickets with retry logic for optimistic locking failures
     * This is the KEY method for handling concurrent booking attempts
     */
    private ReserveTicketsResponse reserveTicketsWithRetry(Long eventId, int quantity) {
        int attempts = 0;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                attempts++;
                log.info("Attempting to reserve {} tickets for event {} (attempt {}/{})",
                        quantity, eventId, attempts, MAX_RETRY_ATTEMPTS);

                ReserveTicketsResponse response = eventGrpcClient.reserveTickets(eventId, quantity);

                if (response.getSuccess()) {
                    log.info("Successfully reserved tickets on attempt {}", attempts);
                    return response;
                } else {
                    // No tickets available - don't retry
                    log.warn("Tickets not available: {}", response.getMessage());
                    return response;
                }

            } catch (StatusRuntimeException e) {
                // Check if this is an optimistic locking failure
                if (e.getStatus().getCode() == io.grpc.Status.Code.ABORTED ||
                    e.getMessage().contains("OptimisticLock")) {

                    if (attempts >= MAX_RETRY_ATTEMPTS) {
                        log.error("Optimistic locking failure after {} attempts", attempts);
                        throw new SeatNotAvailableException(
                                "Unable to reserve tickets due to high demand. Please try again.");
                    }

                    log.warn("Optimistic locking conflict on attempt {}, retrying...", attempts);
                    sleep(RETRY_DELAY_MS * attempts); // Exponential backoff

                } else {
                    // Other error - don't retry
                    log.error("Non-retryable error reserving tickets: {}", e.getMessage());
                    throw e;
                }
            }
        }

        throw new SeatNotAvailableException("Unable to reserve tickets after " + MAX_RETRY_ATTEMPTS + " attempts");
    }

    /**
     * Generates a unique booking reference
     * Format: BK-{UUID short}
     */
    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Sleep helper for retry delays
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }
}
