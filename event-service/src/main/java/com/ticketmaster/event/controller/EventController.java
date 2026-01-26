package com.ticketmaster.event.controller;


import com.ticketmaster.event.dto.request.EventRequest;
import com.ticketmaster.event.service.EventService;
import com.ticketmaster.event.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * <h1>Event Management Controller</h1>
 * <p>
 * Handles the lifecycle of concert events.
 * This controller allows Admins to create events and Users to view/buy tickets.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events and ticket purchases")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves a list of all available events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves a specific event by its unique identifier")
    public ResponseEntity<Event> getEventsById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }


    @PostMapping
    @Operation(summary = "Create new event", description = "Creates a new event (Admin only)")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        Event created = eventService.createEvent(eventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Updates an existing event (Admin only)")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest eventRequest) {
        return ResponseEntity.ok(eventService.updateEvent(id, eventRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Deletes an event (Admin only)")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


}
