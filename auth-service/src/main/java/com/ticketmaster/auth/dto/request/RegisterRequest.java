package com.ticketmaster.auth.dto.request;


import com.ticketmaster.auth.util.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <h1>Registration Request DTO</h1>
 * <p>
 * This object defines the strict contract for creating a new user account.
 * It ensures that no "garbage data" (like empty names or weak passwords) ever
 * reaches the database.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * The user's first name.
     */
    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * The user's last name.
     */
    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * The user's email address.
     * Must be unique in the system (checked by the Service layer).
     */
    @Schema(description = "Unique email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * The user's chosen password.
     * <p>
     * <b>Security Note:</b> We enforce a minimum length of 8 characters here.
     * In a real production app, we would also use a Regex pattern to require
     * numbers and special symbols (e.g., {@code @Pattern(...)})
     * </p>
     */
    @Schema(description = "Strong password for the account", example = "SecurePass123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;


    @Schema(description = "Role of the user", example = "USER")
    @NotNull(message = "Role is required")
    private Role role;



}
