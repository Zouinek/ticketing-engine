package com.ticketmaster.common.dto;

import com.ticketmaster.common.enums.EventCategory;
import com.ticketmaster.common.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shared Event Response DTO used across microservices
 * Used by: event-service, booking-service, notification-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    @Schema(description = "Unique Event ID", example = "1")
    private Long id;

    @Schema(description = "Name of the event", example = "Coldplay Music of the Spheres")
    private String name;

    @Schema(description = "Description of the event", example = "An exhilarating concert experience by Coldplay.")
    private String description;

    @Schema(description = "Date of the event", example = "2026-06-15T20:00:00")
    private LocalDateTime date;

    @Schema(description = "Performer ID", example = "2")
    private Long performerId;

    @Schema(description = "Venue ID where the event is held", example = "3")
    private Long venueId;

    @Schema(description = "Price per ticket", example = "150.00")
    private Double ticketPrice;

    @Schema(description = "Total number of tickets for the event", example = "500")
    private Integer totalTickets;

    @Schema(description = "Tickets currently left to sell", example = "450")
    private Integer availableTickets;

    @Schema(description = "Current status of the event", example = "SCHEDULED")
    private EventStatus status;

    @Schema(description = "Category of the event", example = "MUSIC")
    private EventCategory category;

    @Schema(description = "When the event was created in the system", example = "2026-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the event was last updated", example = "2026-01-15T14:30:00")
    private LocalDateTime updatedAt;
}
