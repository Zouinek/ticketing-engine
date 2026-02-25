package com.ticketmaster.booking.scheduler;


import com.ticketmaster.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to automatically release expired bookings
 * Runs every minute to check for PENDING bookings that have passed their expiration time
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingService bookingService;

    /**
     * Release expired bookings every 1 minute
     * This ensures tickets are returned to inventory if payment isn't completed
     */
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void expirePendingBookings() {
        log.info("Starting scheduled task: Release expired bookings");
        try {
            bookingService.releaseExpiredBookings();
            log.info("Completed scheduled task: Release expired bookings");
        } catch (Exception e) {
            log.error("Error during scheduled task: {}", e.getMessage(), e);
        }
    }

}