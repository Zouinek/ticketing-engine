package com.ticketmaster.event.repository;

import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests for EventRepository
 * Uses @DataJpaTest which provides an in-memory H2 database
 */
@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        eventRepository.deleteAll();

        // Create test events - all with future dates to pass validation
        Event musicEvent = Event.builder()
                .name("Rock Concert")
                .description("Amazing rock concert")
                .date(LocalDateTime.now().plusDays(30))
                .venueId(1L)
                .performerId(1L)
                .ticketPrice(100.0)
                .totalTickets(500)
                .availableTickets(500)
                .status(Status.UPCOMING)
                .category(Category.MUSIC)
                .build();

        Event sportsEvent = Event.builder()
                .name("Football Match")
                .description("Championship final")
                .date(LocalDateTime.now().plusDays(15))
                .venueId(2L)
                .performerId(2L)
                .ticketPrice(75.0)
                .totalTickets(1000)
                .availableTickets(1000)
                .status(Status.UPCOMING)
                .category(Category.SPORTS)
                .build();

        // Completed event with future date (to pass validation) but COMPLETED status
        Event completedEvent = Event.builder()
                .name("Past Concert")
                .description("Completed concert")
                .date(LocalDateTime.now().plusDays(5)) // Future date to pass @Future validation
                .venueId(3L)
                .performerId(3L)
                .ticketPrice(50.0)
                .totalTickets(200)
                .availableTickets(0)
                .status(Status.COMPLETED) // Status indicates it's completed
                .category(Category.MUSIC)
                .build();

        // Save events
        entityManager.persist(musicEvent);
        entityManager.persist(sportsEvent);
        entityManager.persist(completedEvent);
        entityManager.flush();
    }

    @Test
    void shouldFindEventsByStatus_UPCOMING() {
        // When
        List<Event> upcomingEvents = eventRepository.findEventByStatus(Status.UPCOMING);

        // Then
        assertThat(upcomingEvents).hasSize(2);
        assertThat(upcomingEvents).extracting(Event::getStatus)
                .containsOnly(Status.UPCOMING);
        assertThat(upcomingEvents).extracting(Event::getName)
                .contains("Rock Concert", "Football Match");
    }

    @Test
    void shouldFindEventsByStatus_COMPLETED() {
        // When
        List<Event> completedEvents = eventRepository.findEventByStatus(Status.COMPLETED);

        // Then
        assertThat(completedEvents).hasSize(1);
        assertThat(completedEvents.get(0).getName()).isEqualTo("Past Concert");
        assertThat(completedEvents.get(0).getStatus()).isEqualTo(Status.COMPLETED);
    }

    @Test
    void shouldReturnEmptyList_WhenNoEventsMatchStatus() {
        // When
        List<Event> cancelledEvents = eventRepository.findEventByStatus(Status.CANCELLED);

        // Then
        assertThat(cancelledEvents).isEmpty();
    }

    @Test
    void shouldFindEventsByCategory_MUSIC() {
        // When
        List<Event> musicEvents = eventRepository.findEventByCategory(Category.MUSIC);

        // Then
        assertThat(musicEvents).hasSize(2);
        assertThat(musicEvents).extracting(Event::getCategory)
                .containsOnly(Category.MUSIC);
        assertThat(musicEvents).extracting(Event::getName)
                .contains("Rock Concert", "Past Concert");
    }

    @Test
    void shouldFindEventsByCategory_SPORTS() {
        // When
        List<Event> sportsEvents = eventRepository.findEventByCategory(Category.SPORTS);

        // Then
        assertThat(sportsEvents).hasSize(1);
        assertThat(sportsEvents.get(0).getName()).isEqualTo("Football Match");
        assertThat(sportsEvents.get(0).getCategory()).isEqualTo(Category.SPORTS);
    }

    @Test
    void shouldReturnEmptyList_WhenNoEventsMatchCategory() {
        // When
        List<Event> theaterEvents = eventRepository.findEventByCategory(Category.THEATER);

        // Then
        assertThat(theaterEvents).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveEvent() {
        // Given
        Event newEvent = Event.builder()
                .name("Comedy Show")
                .description("Stand-up comedy")
                .date(LocalDateTime.now().plusDays(20))
                .venueId(4L)
                .performerId(4L)
                .ticketPrice(30.0)
                .totalTickets(300)
                .availableTickets(300)
                .status(Status.UPCOMING)
                .category(Category.COMEDY)
                .build();

        // When
        Event savedEvent = eventRepository.save(newEvent);

        // Then
        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getName()).isEqualTo("Comedy Show");
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateEvent() {
        // Given
        Event event = eventRepository.findAll().get(0);
        String originalName = event.getName();

        // When
        event.setName("Updated Event Name");
        Event updatedEvent = eventRepository.save(event);

        // Then
        assertThat(updatedEvent.getName()).isEqualTo("Updated Event Name");
        assertThat(updatedEvent.getName()).isNotEqualTo(originalName);
        assertThat(updatedEvent.getId()).isEqualTo(event.getId());
    }

    @Test
    void shouldDeleteEvent() {
        // Given
        Event event = eventRepository.findAll().get(0);
        Long eventId = event.getId();

        // When
        eventRepository.delete(event);

        // Then
        assertThat(eventRepository.findById(eventId)).isEmpty();
    }

    @Test
    void shouldFindAllEvents() {
        // When
        List<Event> allEvents = eventRepository.findAll();

        // Then
        assertThat(allEvents).hasSize(3);
    }
}
