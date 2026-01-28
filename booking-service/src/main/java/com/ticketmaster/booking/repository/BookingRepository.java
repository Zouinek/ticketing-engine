package com.ticketmaster.booking.repository;

import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.util.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking>  findByBookingStatusAndExpiresAtBefore(BookingStatus bookingStatus, LocalDateTime now);
}
