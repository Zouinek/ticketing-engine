package com.ticketmaster.event;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import com.ticketmaster.event.config.JwtService;

/**
 * Integration test to verify the application context loads successfully
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class EventApplicationTest {

    // Mock the JwtService bean to avoid dependency issues
    @MockBean
    private JwtService jwtService;

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // It verifies that all beans are properly configured
    }
}
