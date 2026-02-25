package com.ticketmaster.booking.exception;

public class BookingExpiredException extends RuntimeException {
    public BookingExpiredException(String message) {
        super(message);
    }
    public BookingExpiredException(Long id) {
        super("Booking with ID: " + id + " has expired");

    }

}
