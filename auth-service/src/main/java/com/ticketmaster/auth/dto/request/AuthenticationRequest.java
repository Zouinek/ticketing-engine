package com.ticketmaster.auth.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>Authentication Request DTO</h1>
 * <p>
 * This object captures the raw login credentials sent by the user (or frontend).
 * It acts as a "Data Carrier" between the HTTP request and the AuthenticationController.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

    /**
     * The user's email address.
     * Must be a valid email format (e.g., user@example.com).
     */
    @Schema(description = "User's email address", example = "admin@ticketmaster.com")
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * The user's raw password.
     * <p>
     * <b>Note:</b> We validate that it is not empty, but generally we avoid strict
     * length checks on <i>Login</i> to support legacy users who might have shorter passwords.
     * </p>
     */
    @Schema(description = "User's password", example = "StrongPass123!")
    @NotBlank(message = "Password is Required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

}
