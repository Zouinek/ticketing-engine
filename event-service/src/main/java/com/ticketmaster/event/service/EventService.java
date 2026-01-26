package com.ticketmaster.event.service;


import com.ticketmaster.event.dto.request.EventRequest;
import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.exception.EventNotFoundException;
import com.ticketmaster.event.repository.EventRepository;
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
     * @throws EventNotFoundException If the event is not found (404).
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Saves a new event to the database.
     * Automatically sets availableTickets to match totalTickets.
     * @param eventRequest The event request DTO containing the event data.
     * @return The persisted event (with generated ID).
     */
    public Event createEvent(EventRequest eventRequest) {
        Event event = Event.builder()
                .name(eventRequest.getName())
                .description(eventRequest.getDescription())
                .date(eventRequest.getDate())
                .venueId(eventRequest.getVenueId())
                .performerId(eventRequest.getPerformerId())
                .ticketPrice(eventRequest.getTicketPrice())
                .totalTickets(eventRequest.getTotalTickets())
                .availableTickets(eventRequest.getTotalTickets()) // Initialize to total
                .status(eventRequest.getStatus())
                .category(eventRequest.getCategory())
                .build();

        return eventRepository.save(event);
    }

    /**
     * Updates an existing event's details.
     * <p>
     * We fetch the existing event first to ensure it exists, then copy the new values over.
     * Note: availableTickets and totalTickets are NOT updated here to prevent ticket count corruption.
     * </p>
     * @param id The ID of the event to update.
     * @param eventRequest The new event data from the request.
     * @return The updated entity.
     */
    public Event updateEvent(Long id, EventRequest eventRequest) {
        Event existingEvent = getEventById(id);

        existingEvent.setName(eventRequest.getName());
        existingEvent.setDescription(eventRequest.getDescription());
        existingEvent.setDate(eventRequest.getDate());
        existingEvent.setVenueId(eventRequest.getVenueId());
        existingEvent.setPerformerId(eventRequest.getPerformerId());
        existingEvent.setTicketPrice(eventRequest.getTicketPrice());
        existingEvent.setStatus(eventRequest.getStatus());
        existingEvent.setCategory(eventRequest.getCategory());

        // Note: We intentionally do NOT update totalTickets or availableTickets
        // to prevent accidental ticket count corruption after sales have started

        return eventRepository.save(existingEvent);
    }

    /**
     * Deletes an event permanently.
     * @param id The ID of the event to remove.
     */
    public void deleteEvent(Long id) {
        Event eventToDelete = getEventById(id);
        eventRepository.delete(eventToDelete);
    }


}
