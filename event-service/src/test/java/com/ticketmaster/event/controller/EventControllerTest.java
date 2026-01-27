package com.ticketmaster.event.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmaster.event.dto.request.EventRequest;
import com.ticketmaster.event.dto.request.EventUpdateRequest;
import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.exception.EventNotFoundException;
import com.ticketmaster.event.service.EventService;

import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EventController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.ticketmaster\\.event\\.config\\..*"
        )
)
@Import(TestSecurityConfig.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;


    @Test
    void getAllEvents_ShouldReturnListOfEvents() throws Exception {

        when(eventService.getAllEvents()).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").build(),
                Event.builder().id(2L).name("Concert B").build()
        ));

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Concert A"))
                .andExpect(jsonPath("$[1].name").value("Concert B"));

    }

    @Test
    void getAllEvents_ShouldReturnEmptyList_WhenNoEventsExist() throws Exception {

        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        Long eventId = 1L;
        Event mockEvent = Event.builder().id(eventId).name("Concert A").build();

        when(eventService.getEventById(eventId)).thenReturn(mockEvent);

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.name").value("Concert A"));


    }

    @Test
    void getEventById_ShouldReturnNotFound_WhenEventDoesNotExist() throws Exception {
        Long eventId = 99L;

        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException(eventId));

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventById_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(get("/api/v1/events/{id}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        EventRequest eventRequest = new EventRequest(
                "Concert A",
                "A great concert",
                LocalDateTime.now().plusDays(30),
                1L,
                2L,
                100.0,
                500,
                Status.UPCOMING,
                Category.MUSIC
        );
        when(eventService.createEvent(eventRequest)).thenReturn(
                Event.builder()
                        .id(1L)
                        .name(eventRequest.getName())
                        .description(eventRequest.getDescription())
                        .date(eventRequest.getDate())
                        .venueId(eventRequest.getVenueId())
                        .performerId(eventRequest.getPerformerId())
                        .ticketPrice(eventRequest.getTicketPrice())
                        .totalTickets(eventRequest.getTotalTickets())
                        .availableTickets(eventRequest.getTotalTickets())
                        .status(eventRequest.getStatus())
                        .category(eventRequest.getCategory())
                        .build()
        );
        mockMvc.perform(post("/api/v1/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Concert A"))
                .andExpect(jsonPath("$.availableTickets").value(500));
    }

    @Test
    void createEvent_ShouldReturnBadRequest_WhenDataIsInvalid() throws Exception {
        EventRequest eventRequest = new EventRequest(
                "", // Invalid name
                "A great concert",
                LocalDateTime.now().minusDays(1), // Past date
                1L,
                2L,
                -100.0, // Negative ticket price
                0, // Zero total tickets
                Status.UPCOMING,
                Category.MUSIC
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        Long eventId = 1L;
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setTicketPrice(300.0);

        Event updatedEvent = Event.builder()
                .id(eventId)
                .name("Concert A")
                .description("A great concert")
                .date(LocalDateTime.now().plusDays(30))
                .venueId(1L)
                .performerId(2L)
                .ticketPrice(300.0)  // Updated price
                .totalTickets(500)
                .availableTickets(500)
                .status(Status.UPCOMING)
                .category(Category.MUSIC)
                .build();

        when(eventService.updateEvent(eventId, updateRequest)).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/v1/events/{id}", eventId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.ticketPrice").value(300.0));

    }

    @Test
    void updateEvent_ShouldReturnNotFound_WhenEventDoesNotExist() throws Exception {
        Long eventId = 99L;
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setName("Updated Name");

        when(eventService.updateEvent(eventId, updateRequest))
                .thenThrow(new EventNotFoundException(eventId));

        mockMvc.perform(put("/api/v1/events/{id}", eventId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(delete("/api/v1/events/{id}", eventId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_ShouldReturnNotFound_WhenEventDoesNotExist() throws Exception {
        Long eventId = 99L;

        doThrow(new EventNotFoundException(eventId)).when(eventService).deleteEvent(eventId);

        mockMvc.perform(delete("/api/v1/events/{id}", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventsByStatus_ShouldReturnFilteredEvents() throws Exception {
        when(eventService.getEventsByStatus(Status.UPCOMING)).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").status(Status.UPCOMING).build(),
                Event.builder().id(2L).name("Concert B").status(Status.UPCOMING).build()
        ));

        mockMvc.perform(get("/api/v1/events/status/{status}", "UPCOMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("UPCOMING"));
    }

    @Test
    void getEventsByCategory_ShouldReturnFilteredEvents() throws Exception {
        when(eventService.getEventsByCategory(Category.MUSIC)).thenReturn(List.of(
                Event.builder().id(1L).name("Concert A").category(Category.MUSIC).build(),
                Event.builder().id(3L).name("Concert C").category(Category.MUSIC).build()
        ));

        mockMvc.perform(get("/api/v1/events/category/{category}", "MUSIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("MUSIC"));
    }


}