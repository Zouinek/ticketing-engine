package com.ticketmaster.event.exception;

/**
 * Exception thrown when trying to buy tickets for a sold-out event
 */
public class NoTicketsAvailableException extends RuntimeException {
    public NoTicketsAvailableException(String message) {
        super(message);
    }

    public NoTicketsAvailableException(String eventName, Long eventId) {
        super(String.format("SOLD OUT: No tickets available for '%s' (Event ID: %d)", eventName, eventId));
    }
}
