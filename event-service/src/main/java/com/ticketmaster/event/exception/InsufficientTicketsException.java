package com.ticketmaster.event.exception;

/**
 * Exception thrown when there are not enough tickets available for reservation
 */
public class InsufficientTicketsException extends RuntimeException {
    public InsufficientTicketsException(String message) {
        super(message);
    }
}
