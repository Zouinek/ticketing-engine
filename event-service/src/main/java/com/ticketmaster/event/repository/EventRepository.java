package com.ticketmaster.event.repository;

import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

    List<Event> findEventByStatus(Status status);

    List<Event> findEventByCategory(Category category);
}
