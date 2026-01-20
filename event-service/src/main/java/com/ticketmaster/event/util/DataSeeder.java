package com.ticketmaster.event.util;

import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * <h1>Database Seeder</h1>
 * <p>
 * This component runs automatically upon application startup to populate the database
 * with initial test data.
 * </p>
 * <h2>Why use this?</h2>
 * <ul>
 * <li><b>Development Speed:</b> Developers don't need to manually insert events via Postman every time they wipe the database.</li>
 * <li><b>Consistency:</b> Ensures every developer (or CI/CD pipeline) starts with the exact same known state.</li>
 * </ul>
 */
@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;

    public DataSeeder(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * The execution method triggered by Spring Boot after the context loads.
     *
     * @param args Command line arguments (unused here).
     * @throws Exception If database access fails.
     */
    @Override
    public void run(String... args) throws Exception {
        // This runs automatically when the app starts
        if (eventRepository.count() == 0) {
            Event testEvent = Event.builder()
                    .name("travis scott- tour ")
                    .location("Munich Olympic Stadium")
                    .date(LocalDateTime.now().plusMonths(3))
                    .ticketPrice(199.99)
                    .totalTickets(1000)
                    .availableTickets(1000)
                    .build();

            eventRepository.save(testEvent);
            log.info("SUCCESS: Test Event inserted into Database!");
        }
    }
}