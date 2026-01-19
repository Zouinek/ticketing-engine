package com.ticketmaster.ticketing_engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <h1>System Status Controller</h1>
 * <p>
 * Provides public endpoints to check the health and connectivity of the API.
 * This is commonly used by Load Balancers (AWS/Kubernetes) and the Frontend
 * to verify the backend is reachable before attempting a login.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System Status", description = "Public health check endpoints")
public class SystemController {
    /**
     * simple "Ping" endpoint.
     * <p>
     * Returns a JSON status object. This is better than a simple string because
     * the Frontend can easily parse it to check version numbers or maintenance flags.
     * </p>
     *
     * @return A HTTP 200 OK with the system status.
     */
    @Operation(summary = "Check API Status", description = "Returns 200 OK if the backend is running.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> sayHello() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Ticketing Engine is running",
                "version", "1.0.0"
        ));
    }
}
