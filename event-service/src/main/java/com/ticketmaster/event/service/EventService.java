package com.ticketmaster.event.service;


import com.ticketmaster.common.enums.EventCategory;
import com.ticketmaster.common.enums.EventStatus;
import com.ticketmaster.event.dto.request.EventRequest;
import com.ticketmaster.event.dto.request.EventUpdateRequest;
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
     * Updates an existing event's details with partial update support.
     * <p>
     * This method only updates the fields that are actually provided in the request.
     * You can update just one field (e.g., only the name) without sending all other fields.
     * </p>
     * <p>
     * <b>Note:</b> totalTickets and availableTickets are intentionally NOT updatable
     * to prevent ticket count corruption after sales have started.
     * </p>
     * @param id The ID of the event to update.
     * @param updateRequest The update request containing only the fields to change.
     * @return The updated entity.
     */
    public Event updateEvent(Long id, EventUpdateRequest updateRequest) {
        Event existingEvent = getEventById(id);

        // Only update fields that are provided (not null)
        if (updateRequest.getName() != null) {
            existingEvent.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            existingEvent.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getDate() != null) {
            existingEvent.setDate(updateRequest.getDate());
        }
        if (updateRequest.getVenueId() != null) {
            existingEvent.setVenueId(updateRequest.getVenueId());
        }
        if (updateRequest.getPerformerId() != null) {
            existingEvent.setPerformerId(updateRequest.getPerformerId());
        }
        if (updateRequest.getTicketPrice() != null) {
            existingEvent.setTicketPrice(updateRequest.getTicketPrice());
        }
        if (updateRequest.getStatus() != null) {
            existingEvent.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getCategory() != null) {
            existingEvent.setCategory(updateRequest.getCategory());
        }

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

    /**
     * Retrieves all events with a specific status.
     * @param status The status to filter by (e.g., UPCOMING, CANCELLED, COMPLETED).
     * @return A list of events with the specified status.
     */
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findEventByStatus(status);
    }

    /**
     * Retrieves all events with a specific category.
     * @param category The category to filter by (e.g., UPCOMING, CANCELLED, COMPLETED).
     * @return A list of events with the specified category.
     */
    public List<Event> getEventsByCategory(EventCategory category) {
        return eventRepository.findEventByCategory(category);
    }


}
