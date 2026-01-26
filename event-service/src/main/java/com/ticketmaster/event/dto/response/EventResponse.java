package com.ticketmaster.event.dto.response;


import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

import java.time.LocalDateTime;


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
    private double ticketPrice;

    @Schema(description = "Tickets currently left to sell", example = "450")
    private int availableTickets;

    @Schema(description = "Current status of the event", example = "SCHEDULED")
    private Status status;

    @Schema(description = "Category of the event", example = "MUSIC")
    private Category category;

    @Schema(description = "When the event was created in the system", example = "2026-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the event was last updated", example = "2026-01-15T14:30:00")
    private LocalDateTime updatedAt;

}