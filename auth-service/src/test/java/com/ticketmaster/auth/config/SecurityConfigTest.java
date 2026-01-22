package com.ticketmaster.auth.config;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowWhitelistedSystemEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/system/status"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
    }

    @Test
    void shouldNotBlockWhitelistedAuthEndpointsBySecurity() throws Exception {
        // We don't care if the endpoint exists or which HTTP method it supports here.
        // We only care that security does NOT respond with 401/403 for the whitelisted path.
        mockMvc.perform(get("/api/v1/auth/does-not-matter"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s == 401 || s == 403) {
                        throw new AssertionError("Expected NOT 401/403 for whitelisted auth path but got " + s);
                    }
                })
                .andExpect(unauthenticated());
    }

    @Test
    void shouldRejectProtectedEndpointsWithoutAuthentication() throws Exception {
        // Depending on how the app configures AuthenticationEntryPoint, unauthenticated can be 401 or 403.
        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldAllowProtectedEndpointsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }
}
