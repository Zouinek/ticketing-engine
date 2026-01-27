package com.ticketmaster.event.exception;

/**
 * Exception thrown when an event is not found by ID
 */
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }

    public EventNotFoundException(Long id) {
        super("Event not found with ID: " + id);
    }
}
