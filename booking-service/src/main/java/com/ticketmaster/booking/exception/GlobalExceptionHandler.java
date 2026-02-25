package com.ticketmaster.booking.exception;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookingNotFound(BookingNotFoundException ex) {
        log.error("Booking not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleSeatNotAvailable(SeatNotAvailableException ex) {
        log.error("Seat not available: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BookingExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleBookingExpired(BookingExpiredException ex) {
        log.error("Booking expired: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ResponseEntity<Map<String, Object>> handleBookingAlreadyCancelled(BookingAlreadyCancelledException ex) {
        log.error("Booking already cancelled: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        log.error("Optimistic locking failure: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Unable to process booking due to concurrent updates. Please try again."
        );
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGrpcException(StatusRuntimeException ex) {
        log.error("gRPC error: {} - {}", ex.getStatus(), ex.getMessage());

        return switch (ex.getStatus().getCode()) {
            case NOT_FOUND -> buildErrorResponse(HttpStatus.NOT_FOUND, "Event not found");
            case UNAVAILABLE -> buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event service is temporarily unavailable. Please try again later.");
            case ABORTED -> buildErrorResponse(HttpStatus.CONFLICT,
                    "Unable to reserve tickets due to high demand. Please try again.");
            default -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while processing your request");
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
