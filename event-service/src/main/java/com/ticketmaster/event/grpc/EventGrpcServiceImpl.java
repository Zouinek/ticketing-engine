package com.ticketmaster.event.grpc;

import com.ticketmaster.common.grpc.*;
import com.ticketmaster.event.entity.Event;
import com.ticketmaster.event.exception.EventNotFoundException;
import com.ticketmaster.event.exception.InsufficientTicketsException;
import com.ticketmaster.event.repository.EventRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

/**
 * gRPC Service Implementation for Event Service
 * Handles event information and ticket reservation via gRPC
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventGrpcServiceImpl extends EventServiceGrpc.EventServiceImplBase {

    private final EventRepository eventRepository;

    /**
     * Get event details by ID
     */
    @Override
    public void getEvent(GetEventRequest request, StreamObserver<GetEventResponse> responseObserver) {
        log.info("gRPC: Received GetEvent request for eventId: {}", request.getEventId());

        try {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + request.getEventId()));

            GetEventResponse response = GetEventResponse.newBuilder()
                    .setEventId(event.getId())
                    .setName(event.getName())
                    .setStatus(mapToGrpcStatus(event.getStatus()))
                    .setAvailableTickets(event.getAvailableTickets())
                    .setTicketPrice(event.getTicketPrice())
                    .setTotalTickets(event.getTotalTickets())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: Successfully returned event details for eventId: {}", request.getEventId());

        } catch (EventNotFoundException e) {
            log.error("gRPC: Event not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC: Error retrieving event: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Reserve tickets for an event
     */
    @Override
    @Transactional
    public void reserveTickets(ReserveTicketsRequest request, StreamObserver<ReserveTicketsResponse> responseObserver) {
        log.info("gRPC: Received ReserveTickets request for eventId: {}, quantity: {}",
                request.getEventId(), request.getQuantity());

        try {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + request.getEventId()));

            // Check if enough tickets are available
            if (event.getAvailableTickets() < request.getQuantity()) {
                throw new InsufficientTicketsException(
                        String.format("Only %d tickets available, requested %d",
                                event.getAvailableTickets(), request.getQuantity()));
            }

            // Reserve tickets
            event.setAvailableTickets(event.getAvailableTickets() - request.getQuantity());
            eventRepository.save(event);

            ReserveTicketsResponse response = ReserveTicketsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Tickets reserved successfully")
                    .setRemainingTickets(event.getAvailableTickets())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: Successfully reserved {} tickets for eventId: {}", request.getQuantity(), request.getEventId());

        } catch (EventNotFoundException e) {
            log.error("gRPC: Event not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());

        } catch (InsufficientTicketsException e) {
            log.error("gRPC: Insufficient tickets: {}", e.getMessage());
            ReserveTicketsResponse response = ReserveTicketsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .setRemainingTickets(0)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC: Error reserving tickets: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Map JPA Status enum to gRPC EventStatus enum
     */
    private com.ticketmaster.common.grpc.EventStatus mapToGrpcStatus(com.ticketmaster.common.enums.EventStatus jpaStatus) {
        return switch (jpaStatus) {
            case SCHEDULED -> com.ticketmaster.common.grpc.EventStatus.SCHEDULED;
            case UPCOMING -> com.ticketmaster.common.grpc.EventStatus.UPCOMING;
            case COMPLETED -> com.ticketmaster.common.grpc.EventStatus.COMPLETED;
            case CANCELLED -> com.ticketmaster.common.grpc.EventStatus.CANCELLED;
        };
    }
}
