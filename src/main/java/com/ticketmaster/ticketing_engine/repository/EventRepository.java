package com.ticketmaster.ticketing_engine.repository;

import com.ticketmaster.ticketing_engine.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    //

}
