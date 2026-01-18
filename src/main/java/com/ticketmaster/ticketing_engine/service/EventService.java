package com.ticketmaster.ticketing_engine.service;


import com.ticketmaster.ticketing_engine.entity.Event;
import com.ticketmaster.ticketing_engine.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }
    public Event updateEvent(Long id, Event event) {
        Event oldEvent = getEventById(id);

        oldEvent.setName(event.getName());
        oldEvent.setDate(event.getDate());
        oldEvent.setLocation(event.getLocation());
        oldEvent.setTicketPrice(event.getTicketPrice());
        oldEvent.setTotalTickets(event.getTotalTickets());

        return eventRepository.save(oldEvent);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public String buyTicket(long eventId) {
        Event event = getEventById(eventId);

        if(event.getTotalTickets() >= 0) {
            event.setTotalTickets(event.getTotalTickets() - 1);
            eventRepository.save(event);
            return "You Bought the Ticket for " + event.getName();
        }else {
             throw new RuntimeException("SOLD OUT: No tickets left for this event");
        }
    }
}
