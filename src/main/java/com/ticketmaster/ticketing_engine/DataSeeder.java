package com.ticketmaster.ticketing_engine;

import com.ticketmaster.ticketing_engine.entity.Event;
import com.ticketmaster.ticketing_engine.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;\

    public DataSeeder(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // This runs automatically when the app starts
        if (eventRepository.count() == 0) {
            Event testEvent = Event.builder()
                    .name("Taylor Swift - Eras Tour")
                    .location("Munich Olympic Stadium")
                    .data(LocalDateTime.now().plusMonths(3))
                    .build();

            eventRepository.save(testEvent);
            System.out.println("✅ SUCCESS: Test Event inserted into Database!");
        }
    }
}