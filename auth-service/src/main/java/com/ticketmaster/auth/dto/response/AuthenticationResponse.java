package com.ticketmaster.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>Authentication Response DTO</h1>
 * <p>
 * This object is sent back to the client after a successful Login or Registration.
 * It contains the <b>JSON Web Token (JWT)</b> that the client must save.
 * </p>
 * <h2>Client Responsibility:</h2>
 * <p>
 * The Frontend (React/Angular) must:
 * <ol>
 * <li>Receive this JSON.</li>
 * <li>Extract the {@code token}.</li>
 * <li>Save it in LocalStorage or Cookies.</li>
 * <li>Attach it to the header of every future request: {@code Authorization: Bearer <token>}</li>
 * </ol>
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    /**
     * The JWT Access Token.
     * <p>
     * This string contains the user's identity (email, role) signed cryptographically
     * by the server. It typically expires after 24 hours.
     * </p>
     */

    @Schema(description = "JWT Access Token. format: eyJhbGciOiJIUzI1Ni...", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pb...")
    private String token;

}
