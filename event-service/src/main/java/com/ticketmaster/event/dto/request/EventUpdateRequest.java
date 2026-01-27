package com.ticketmaster.event.dto.request;


import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <h1>Event Update Request DTO</h1>
 * <p>
 * This DTO is used for partial updates of events.
 * All fields are optional - only the fields that are provided will be updated.
 * This allows you to update a single attribute without sending the entire event.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {

    @Schema(description = "Event name", example = "Rock Concert 2026")
    @Size(min = 3, max = 255, message = "Event name must be between 3 and 255 characters")
    private String name;

    @Schema(description = "Event description", example = "An amazing rock concert featuring top artists")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Event date and time", example = "2026-06-15T20:00:00")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @Schema(description = "Venue ID where the event will take place", example = "1")
    @Positive(message = "Venue ID must be positive")
    private Long venueId;

    @Schema(description = "Performer ID for the event", example = "1")
    @Positive(message = "Performer ID must be positive")
    private Long performerId;

    @Schema(description = "Price per ticket", example = "50.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
    private Double ticketPrice;

    @Schema(description = "Status of the event", example = "SCHEDULED", allowableValues = {"SCHEDULED", "UPCOMING", "CANCELLED", "COMPLETED"})
    private Status status;

    @Schema(description = "Category of the event", example = "MUSIC", allowableValues = {"MUSIC", "SPORTS", "THEATER", "COMEDY", "CONFERENCE"})
    private Category category;
}
