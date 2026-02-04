package com.ticketmaster.booking.exception;

public class BookingAlreadyCancelledException extends RuntimeException {
    public BookingAlreadyCancelledException(String message) {
        super(message);
    }
    public BookingAlreadyCancelledException(Long id) {
        super("Booking with ID " + id + " is already cancelled");
    }

}
