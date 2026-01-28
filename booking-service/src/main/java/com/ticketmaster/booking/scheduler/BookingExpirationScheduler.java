package com.ticketmaster.booking.scheduler;


import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.repository.BookingRepository;
import com.ticketmaster.booking.util.BookingStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    @Transactional
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void expirePendingBookings() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings = bookingRepository.findByBookingStatusAndExpiresAtBefore(BookingStatus.PENDING, now);
        for (Booking booking : expiredBookings) {
            booking.setBookingStatus(BookingStatus.EXPIRED);
            booking.setCancellationReason("Booking expired - Payment not completed");
            booking.setCancellationDate(now);
            bookingRepository.save(booking);
        }

        log.info("Expired {} pending bookings", expiredBookings.size());
    }


}