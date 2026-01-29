package com.ticketmaster.event.service;

import com.ticketmaster.common.enums.EventCategory;
import com.ticketmaster.common.enums.EventStatus;
import com.ticketmaster.event.dto.request.EventRequest;
import com.ticketmaster.event.dto.request.EventUpdateRequest;
import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.exception.EventNotFoundException;
import com.ticketmaster.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldReturnAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").build(),
                Event.builder().id(2L).name("Concert B").build()
        ));

        List<Event> events = eventService.getAllEvents();

        assert (events.size() == 2);
    }

    @Test
    void shouldReturnEventById() {
        Long eventId = 1L;
        Event mockEvent = Event.builder().id(eventId).name("Concert A").build();
        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(mockEvent));

        Event event = eventService.getEventById(eventId);

        assert (event.getId().equals(eventId));
        assert (event.getName().equals("Concert A"));
    }

    @Test
    void shouldthrowEventNotFoundExceptionWhenEventDoesNotExist() {
        Long eventId = 99L;
        when(eventRepository.findById(eventId)).thenReturn(empty());

        try {
            eventService.getEventById(eventId);
            assert (false); // Should not reach here
        } catch (Exception e) {
            assert (e instanceof EventNotFoundException);
        }

    }

    @Test
    void shouldCreateEvent() {
        EventRequest eventRequest = new EventRequest(
                "Concert A",
                "A great concert",
                LocalDateTime.now().plusDays(30),
                1L,
                2L,
                100.0,
                500,
                EventStatus.UPCOMING,
                EventCategory.MUSIC
        );

        Event savedEvent = Event.builder()
                .id(1L)
                .name("Concert A")
                .description("A great concert")
                .date(eventRequest.getDate())
                .venueId(1L)
                .performerId(2L)
                .ticketPrice(100.0)
                .totalTickets(500)
                .availableTickets(500)
                .status(EventStatus.UPCOMING)
                .category(EventCategory.MUSIC)
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);


        Event createdEvent = eventService.createEvent(eventRequest);


        assert (createdEvent.getId().equals(1L));
        assert (createdEvent.getName().equals("Concert A"));
        assert (createdEvent.getDescription().equals("A great concert"));
        assert (createdEvent.getTotalTickets() == 500);
        assert (createdEvent.getAvailableTickets() == 500);
    }

    @Test
    void shouldReturnEventsByStatus() {

        EventStatus status = EventStatus.UPCOMING;
        when(eventRepository.findEventByStatus(status)).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").status(status).build(),
                Event.builder().id(3L).name("Concert C").status(status).build()
        ));


        List<Event> events = eventService.getEventsByStatus(status);

        assert (events.size() == 2);
        assert (events.get(0).getStatus() == EventStatus.UPCOMING);
        assert (events.get(1).getStatus() == EventStatus.UPCOMING);
    }

    @Test
    void shouldReturnEventsByCategory() {

        EventCategory category = EventCategory.MUSIC;
        when(eventRepository.findEventByCategory(category)).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").category(category).build(),
                Event.builder().id(4L).name("Concert D").category(category).build()
        ));

        List<Event> events = eventService.getEventsByCategory(category);

        assert (events.size() == 2);
        assert (events.get(0).getCategory() == EventCategory.MUSIC);
        assert (events.get(1).getCategory() == EventCategory.MUSIC);
    }

    @Test
    void shouldUpdateEvent() {

        Long eventId = 1L;
        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Concert A")
                .description("A great concert")
                .date(LocalDateTime.now().plusDays(30))
                .venueId(1L)
                .performerId(2L)
                .ticketPrice(100.0)
                .totalTickets(500)
                .availableTickets(500)
                .status(EventStatus.UPCOMING)
                .category(EventCategory.MUSIC)
                .build();

        // Create update request - only updating ticket price
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setTicketPrice(300.0);

        // Create the expected updated event
        Event updatedEvent = Event.builder()
                .id(eventId)
                .name("Concert A")
                .description("A great concert")
                .date(existingEvent.getDate())
                .venueId(1L)
                .performerId(2L)
                .ticketPrice(300.0)  // Updated price
                .totalTickets(500)
                .availableTickets(500)
                .status(EventStatus.UPCOMING)
                .category(EventCategory.MUSIC)
                .build();

        // Mock the getEventById call (used internally by updateEvent)
        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(existingEvent));
        // Mock the save call
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        // Act
        Event result = eventService.updateEvent(eventId, updateRequest);

        // Assert
        assert (result.getId().equals(eventId));
        assert (result.getName().equals("Concert A"));
        assert (result.getTicketPrice() == 300.0);  // Verify the update
        assert (result.getTotalTickets() == 500);  // Verify unchanged fields
    }

    @Test
    void shouldThrowEventNotFoundExceptionWhenUpdatingNonExistentEvent() {
        Long eventId = 99L;
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setTicketPrice(300.0);

        when(eventRepository.findById(eventId)).thenReturn(empty());

        try {
            eventService.updateEvent(eventId, updateRequest);
            assert (false); // Should not reach here
        } catch (Exception e) {
            assert (e instanceof EventNotFoundException);
        }
    }

    @Test
    void shouldDeleteEvent() {
        // Arrange
        Long eventId = 1L;
        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Concert A")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(java.util.Optional.of(existingEvent));

        // Act
        eventService.deleteEvent(eventId);

        // Assert - Verify that delete was called with the correct event
        verify(eventRepository).delete(existingEvent);
    }

    @Test
    void shouldThrowEventNotFoundExceptionWhenDeletingNonExistentEvent() {
        // Arrange
        Long eventId = 99L;
        when(eventRepository.findById(eventId)).thenReturn(empty());

        // Act & Assert
        try {
            eventService.deleteEvent(eventId);
            assert (false); // Should not reach here
        } catch (Exception e) {
            assert (e instanceof EventNotFoundException);
        }
    }
}
