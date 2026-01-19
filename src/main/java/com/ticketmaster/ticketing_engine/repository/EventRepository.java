package com.ticketmaster.ticketing_engine.repository;

import com.ticketmaster.ticketing_engine.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h1>Event Repository</h1>
 * <p>
 * This interface acts as the <b>Data Access Object (DAO)</b> for the Event entity.
 * It handles all interactions with the {@code events} table in the database.
 * </p>
 * <h2>How it works:</h2>
 * <p>
 * By extending {@link JpaRepository}, Spring Data automatically generates the implementation
 * of this interface at runtime. We immediately get access to standard methods like:
 * <ul>
 * <li>{@code save(Event e)} - Create or Update</li>
 * <li>{@code findById(Long id)} - Read one</li>
 * <li>{@code findAll()} - Read all</li>
 * <li>{@code deleteById(Long id)} - Delete</li>
 * </ul>
 * </p>
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * PRO TIP: Custom Query Example
     * <p>
     * You don't need to write SQL! Spring derives the query from the method name.
     * This method would find all events that are not sold out.
     * </p>
     *
     * @param count The minimum number of tickets required (usually 0).
     * @return A list of events with tickets still available.
     */
    // List<Event> findByAvailableTicketsGreaterThan(int count);

}
