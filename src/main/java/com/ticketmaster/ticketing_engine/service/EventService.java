package com.ticketmaster.ticketing_engine.service;


import com.ticketmaster.ticketing_engine.entity.Event;
import com.ticketmaster.ticketing_engine.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Event Business Logic Service</h1>
 * <p>
 * Handles the core operations for managing events (Concerts, Shows).
 * </p>
 * <h2>Current Implementation Status:</h2>
 * <p>
 * This service currently implements standard <b>CRUD</b> (Create, Read, Update, Delete) operations.
 * <br>
 * <b>Note:</b> The {@code buyTicket} method is currently using a naive implementation.
 * It will be upgraded to support <b>Optimistic Locking</b> and high concurrency in the
 * {@code feature/concurrency} branch.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    /**
     * Retrieves all events from the database.
     * @return A list of all stored events.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Finds a specific event by ID.
     * @param id The unique event ID.
     * @return The Event entity.
     * @throws RuntimeException If the event is not found (404).
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    /**
     * Saves a new event to the database.
     * @param event The event data to save.
     * @return The persisted event (with generated ID).
     */

    public Event createEvent(Event event) {
        // TODO: In the future, initialize 'availableTickets' to match 'totalTickets' here.
        return eventRepository.save(event);
    }

    /**
     * Updates an existing event's details.
     * <p>
     * We fetch the existing event first to ensure it exists, then copy the new values over.
     * </p>
     * @param id The ID of the event to update.
     * @param event The new data.
     * @return The updated entity.
     */
    public Event updateEvent(Long id, Event event) {
        Event oldEvent = getEventById(id);

        oldEvent.setName(event.getName());
        oldEvent.setDate(event.getDate());
        oldEvent.setLocation(event.getLocation());
        oldEvent.setTicketPrice(event.getTicketPrice());
        oldEvent.setTotalTickets(event.getTotalTickets());

        return eventRepository.save(oldEvent);
    }

    /**
     * Deletes an event permanently.
     * @param id The ID of the event to remove.
     */
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }


    /**
     * Processes a ticket purchase.
     * <p>
     * <b>Warning:</b> This method currently decreases the {@code totalTickets} count directly.
     * This is a temporary logic placeholder.
     * </p>
     * <h3>Future Upgrade:</h3>
     * When we implement the High-Performance engine, this method will be replaced to use:
     * <ul>
     * <li>{@code availableTickets} instead of {@code totalTickets}</li>
     * <li>Optimistic Locking ({@code @Version}) checking</li>
     * <li>Transaction Retries</li>
     * </ul>
     *
     * @param eventId The ID of the event.
     * @return A success message string.
     * @throws RuntimeException If tickets are sold out.
     */
    public String buyTicket(long eventId) {
        Event event = getEventById(eventId);

        if(event.getTotalTickets() > 0) {

            // TODO: Switch this to event.setAvailableTickets(...) in the next feature branch
            event.setTotalTickets(event.getTotalTickets() - 1);

            eventRepository.save(event);
            return "You Bought the Ticket for " + event.getName();
        }else {
             throw new RuntimeException("SOLD OUT: No tickets left for this event");
        }
    }
}
