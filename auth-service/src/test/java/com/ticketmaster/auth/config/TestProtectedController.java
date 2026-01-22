package com.ticketmaster.auth.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller used by SecurityConfigTest.
 *
 * It exposes endpoints that are NOT whitelisted in SecurityConfig so we can assert
 * that unauthenticated requests are rejected.
 */
@RestController
@RequestMapping("/test")
class TestProtectedController {

    @GetMapping("/protected")
    ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("ok");
    }
}
