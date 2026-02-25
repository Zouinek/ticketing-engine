package com.ticketmaster.booking.exception;

public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
    public SeatNotAvailableException(Long id) {
        super("Seat " + id + " is not available");
    }
}
