package com.ticketmaster.auth.repository;

import com.ticketmaster.auth.entity.User;
import com.ticketmaster.auth.util.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TestUserRepository {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenUserExists() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .email(email)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail(email);

        // Assert
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByEmail_returnsEmpty_whenUserDoesNotExist() {
        // Act
        Optional<User> found = userRepository.findByEmail("missing@example.com");

        // Assert
        Assertions.assertThat(found).isEmpty();
    }
}
