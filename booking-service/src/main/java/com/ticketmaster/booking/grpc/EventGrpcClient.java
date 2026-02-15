package com.ticketmaster.booking.grpc;

import com.ticketmaster.common.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * gRPC Client for Event Service
 * Handles communication with event-service via gRPC
 */
@Slf4j
@Service
public class EventGrpcClient {

    @GrpcClient("event-service")
    private EventServiceGrpc.EventServiceBlockingStub eventServiceStub;

    /**
     * Get event details by ID
     * @param eventId The ID of the event
     * @return Event details response
     * @throws StatusRuntimeException if the event is not found or an error occurs
     */
    public GetEventResponse getEvent(Long eventId) {
        log.info("gRPC Client: Requesting event details for eventId: {}", eventId);

        try {
            GetEventRequest request = GetEventRequest.newBuilder()
                    .setEventId(eventId)
                    .build();

            GetEventResponse response = eventServiceStub.getEvent(request);
            log.info("gRPC Client: Successfully received event details for eventId: {}", eventId);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC Client: Failed to get event {}: {} - {}", eventId, e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * Reserve tickets for an event
     * @param eventId The ID of the event
     * @param quantity Number of tickets to reserve
     * @return Reservation response
     * @throws StatusRuntimeException if the reservation fails
     */
    public ReserveTicketsResponse reserveTickets(Long eventId, int quantity) {
        log.info("gRPC Client: Requesting to reserve {} tickets for eventId: {}", quantity, eventId);

        try {
            ReserveTicketsRequest request = ReserveTicketsRequest.newBuilder()
                    .setEventId(eventId)
                    .setQuantity(quantity)
                    .build();

            ReserveTicketsResponse response = eventServiceStub.reserveTickets(request);
            log.info("gRPC Client: Reservation response - success: {}, message: {}, remaining: {}",
                    response.getSuccess(), response.getMessage(), response.getRemainingTickets());
            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC Client: Failed to reserve tickets for event {}: {} - {}",
                    eventId, e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * Check if event exists and has enough tickets
     * @param eventId The ID of the event
     * @param quantity Number of tickets needed
     * @return true if event exists and has enough tickets
     */
    public boolean validateEventAndTickets(Long eventId, int quantity) {
        try {
            GetEventResponse event = getEvent(eventId);

            if (event.getStatus() != EventStatus.UPCOMING) {
                log.warn("gRPC Client: Event {} is not in UPCOMING status: {}", eventId, event.getStatus());
                return false;
            }

            if (event.getAvailableTickets() < quantity) {
                log.warn("gRPC Client: Event {} has insufficient tickets. Available: {}, Requested: {}",
                        eventId, event.getAvailableTickets(), quantity);
                return false;
            }

            return true;

        } catch (StatusRuntimeException e) {
            log.error("gRPC Client: Event validation failed for eventId {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    /**
     * Release tickets back to available pool
     * Called when bookings expire or are cancelled
     * @param eventId The ID of the event
     * @param quantity Number of tickets to release
     * @return Release response
     * @throws StatusRuntimeException if the release fails
     */
    public ReleaseTicketsResponse releaseTickets(Long eventId, int quantity) {
        log.info("gRPC Client: Requesting to release {} tickets for eventId: {}", quantity, eventId);

        try {
            ReleaseTicketsRequest request = ReleaseTicketsRequest.newBuilder()
                    .setEventId(eventId)
                    .setQuantity(quantity)
                    .build();

            ReleaseTicketsResponse response = eventServiceStub.releaseTickets(request);
            log.info("gRPC Client: Release response - success: {}, message: {}, remaining: {}",
                    response.getSuccess(), response.getMessage(), response.getRemainingTickets());
            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC Client: Failed to release tickets for event {}: {} - {}",
                    eventId, e.getStatus(), e.getMessage());
            throw e;
        }
    }
}
