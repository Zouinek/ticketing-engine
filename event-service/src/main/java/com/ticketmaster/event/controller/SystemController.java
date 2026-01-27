package com.ticketmaster.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * System/Health endpoint controller
 */
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Event Service is running");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }
}
