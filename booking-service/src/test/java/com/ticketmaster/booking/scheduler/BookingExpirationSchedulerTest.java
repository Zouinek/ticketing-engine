package com.ticketmaster.booking.scheduler;

import com.ticketmaster.booking.entity.Booking;
import com.ticketmaster.booking.repository.BookingRepository;
import com.ticketmaster.booking.util.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingExpirationSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingExpirationScheduler scheduler;

    private Booking expiredBooking1;
    private Booking expiredBooking2;

    @BeforeEach
    void setUp() {
        // Create test bookings that should expire
        expiredBooking1 = Booking.builder()
                .id(1L)
                .userId(100L)
                .eventId(200L)
                .paymentId("PAY123456789")
                .bookingStatus(BookingStatus.PENDING)
                .numberOfTickets(2)
                .totalPrice(100.0)
                .bookingReference("BK-001")
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // Expired 5 minutes ago
                .build();

        expiredBooking2 = Booking.builder()
                .id(2L)
                .userId(101L)
                .eventId(201L)
                .paymentId("PAY123457689")
                .bookingStatus(BookingStatus.PENDING)
                .numberOfTickets(3)
                .totalPrice(150.0)
                .bookingReference("BK-002")
                .expiresAt(LocalDateTime.now().minusMinutes(10)) // Expired 10 minutes ago
                .build();
    }

    @Test
    void shouldExpireBookingsWhenExpiredBookingsExist() {
        // Given: There are expired bookings
        List<Booking> expiredBookings = Arrays.asList(expiredBooking1, expiredBooking2);
        when(bookingRepository.findByBookingStatusAndExpiresAtBefore(
                eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(expiredBookings);

        // When: Scheduler runs
        scheduler.expirePendingBookings();

        // Then: All expired bookings should be updated
        verify(bookingRepository).findByBookingStatusAndExpiresAtBefore(
                eq(BookingStatus.PENDING), any(LocalDateTime.class));

        // Verify each booking was saved
        verify(bookingRepository, times(2)).save(any(Booking.class));

        // Verify status was changed to EXPIRED
        assert expiredBooking1.getBookingStatus() == BookingStatus.EXPIRED;
        assert expiredBooking2.getBookingStatus() == BookingStatus.EXPIRED;

        // Verify cancellation reason was set
        assert expiredBooking1.getCancellationReason().equals("Booking expired - Payment not completed");
        assert expiredBooking2.getCancellationReason().equals("Booking expired - Payment not completed");

        // Verify cancellation date was set
        assert expiredBooking1.getCancellationDate() != null;
        assert expiredBooking2.getCancellationDate() != null;
    }

    @Test
    void shouldNotExpireBookingsWhenNoExpiredBookingsExist() {
        // Given: No expired bookings exist
        when(bookingRepository.findByBookingStatusAndExpiresAtBefore(
                eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When: Scheduler runs
        scheduler.expirePendingBookings();

        // Then: No bookings should be saved
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldOnlyExpirePendingBookings() {
        // Given: Repository is called with PENDING status
        scheduler.expirePendingBookings();

        // Then: Verify it only looks for PENDING bookings
        verify(bookingRepository).findByBookingStatusAndExpiresAtBefore(
                eq(BookingStatus.PENDING), any(LocalDateTime.class));
    }

    @Test
    void shouldSetCancellationDateToCurrentTime() {
        // Given: There is one expired booking
        LocalDateTime beforeTest = LocalDateTime.now();
        when(bookingRepository.findByBookingStatusAndExpiresAtBefore(
                eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(expiredBooking1));

        // When: Scheduler runs
        scheduler.expirePendingBookings();
        LocalDateTime afterTest = LocalDateTime.now();

        // Then: Cancellation date should be set to current time
        assert expiredBooking1.getCancellationDate() != null;
        assert !expiredBooking1.getCancellationDate().isBefore(beforeTest);
        assert !expiredBooking1.getCancellationDate().isAfter(afterTest);
    }
}
