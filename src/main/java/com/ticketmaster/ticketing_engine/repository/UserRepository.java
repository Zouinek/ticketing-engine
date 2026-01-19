package com.ticketmaster.ticketing_engine.repository;

import com.ticketmaster.ticketing_engine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 *
 */
public interface UserRepository extends JpaRepository<User, Integer> {


    Optional<User> findByEmail(String email);
}
