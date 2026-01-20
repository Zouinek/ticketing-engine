package com.ticketmaster.event.controller;


import com.ticketmaster.event.service.EventService;
import com.ticketmaster.event.entity.Event;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEventsById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }


    @PostMapping
    public Event createEvent(@RequestBody Event newEvent) {
        return eventService.createEvent(newEvent);
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event newEvent) {
        return eventService.updateEvent(id,newEvent);
    }
    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @GetMapping("/{id}/buy")
    public String buyTicket(@PathVariable Long id) {
        return eventService.buyTicket(id);
    }

}
