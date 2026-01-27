package com.ticketmaster.event.exception;

import com.ticketmaster.event.controller.EventController;
import com.ticketmaster.event.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for GlobalExceptionHandler
 * Uses @WebMvcTest to test exception handling in the controller layer
 */
@WebMvcTest(
        controllers = EventController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.ticketmaster\\.event\\.config\\..*"
        )
)
@Import({GlobalExceptionHandler.class, com.ticketmaster.event.controller.TestSecurityConfig.class})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void shouldHandleEventNotFoundException() throws Exception {
        // Given
        Long eventId = 999L;
        when(eventService.getEventById(eventId))
                .thenThrow(new EventNotFoundException(eventId));

        // When & Then
        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found with ID: 999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() throws Exception {
        // Given - passing a string instead of Long for event ID
        String invalidId = "invalid-id";

        // When & Then
        mockMvc.perform(get("/api/v1/events/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        // Given - invalid event creation request (empty JSON)
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/events")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        // Given
        Long eventId = 1L;
        when(eventService.getEventById(eventId))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        // When & Then
        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
