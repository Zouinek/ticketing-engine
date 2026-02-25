package com.ticketmaster.booking.scheduler;

import com.ticketmaster.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingExpirationSchedulerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingExpirationScheduler scheduler;

    @Test
    void shouldCallBookingServiceToReleaseExpiredBookings() {
        // When: Scheduler runs
        scheduler.expirePendingBookings();

        // Then: Verify BookingService's releaseExpiredBookings method is called
        verify(bookingService, times(1)).releaseExpiredBookings();
    }

    @Test
    void shouldNotThrowWhenBookingServiceThrows() {
        // Given: BookingService throws an exception
        doThrow(new RuntimeException("boom")).when(bookingService).releaseExpiredBookings();

        // When: Scheduler runs
        scheduler.expirePendingBookings();

        // Then: Verify BookingService's releaseExpiredBookings method is called
        verify(bookingService, times(1)).releaseExpiredBookings();
    }
}
