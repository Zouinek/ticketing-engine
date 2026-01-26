package com.ticketmaster.event.dto.request;


import com.ticketmaster.event.util.Category;
import com.ticketmaster.event.util.Status;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 255, message = "Event name must be between 3 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Venue ID is required")
    @Positive(message = "Venue ID must be positive")
    private Long venueId;

    @NotNull(message = "Performer ID is required")
    @Positive(message = "Performer ID must be positive")
    private Long performerId;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
    private Double ticketPrice;

    @NotNull(message = "Total tickets is required")
    @Min(value = 1, message = "Total tickets must be at least 1")
    private Integer totalTickets;

    @NotNull(message = "Event status is required")
    private Status status;

    @NotNull(message = "Event category is required")
    private Category category;
}
