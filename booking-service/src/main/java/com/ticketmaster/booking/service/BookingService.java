package com.ticketmaster.booking.service;

import com.ticketmaster.booking.dto.request.BookingRequest;
import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.exception.BookingNotFoundException;
import com.ticketmaster.booking.repository.BookingRepository;
import com.ticketmaster.common.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;


    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
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

    public Booking createBooking(BookingRequest bookingRequest) {
        // TODO: Calculate price from event-service using EventServiceClient
        BigDecimal calculatedPrice = BigDecimal.valueOf(bookingRequest.getQuantity() * 100.0); // Placeholder price

        Booking booking = Booking.builder()
                .userId(bookingRequest.getUserId())
                .eventId(bookingRequest.getEventId())
                .seatId(bookingRequest.getSeatId())
                .numberOfTickets(bookingRequest.getQuantity())
                .totalPrice(calculatedPrice)
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .bookingReference(generateBookingReference())
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10 min expiration
                .build();
        return bookingRepository.save(booking);
    }

    /**
     * Generates a unique booking reference
     * Format: BK-{UUID short}
     */
    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


}
