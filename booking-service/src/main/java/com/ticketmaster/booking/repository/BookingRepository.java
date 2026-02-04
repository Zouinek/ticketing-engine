package com.ticketmaster.booking.repository;

import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookingStatusAndExpiresAtBefore(BookingStatus bookingStatus, LocalDateTime now);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByEventId(Long eventId);

    List<Booking> findByBookingStatus(BookingStatus status);
}
