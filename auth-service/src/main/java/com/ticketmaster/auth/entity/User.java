package com.ticketmaster.auth.entity;


import com.ticketmaster.auth.util.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * <h1>User Entity</h1>
 * <p>
 * Represents a registered user in the system.
 * </p>
 * <h2>Integration with Spring Security:</h2>
 * <p>
 * This class implements the {@link UserDetails} interface. This is crucial because it allows
 * Spring Security to understand our custom database user. It acts as an adapter,
 * converting our database fields (email, role) into standard security attributes.
 * </p>
 * <h2>Database Schema:</h2>
 * <p>
 * <b>Note:</b> The table is named {@code _user} because "user" is a reserved keyword
 * in PostgreSQL. Using the standard name would cause a syntax error during table creation.
 * </p>
 */
@Data
@Entity
@Table(name = "_user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique User ID", example = "101")
    private Long id;

    @Schema(description = "First Name", example = "Alice")
    private String firstName;

    @Schema(description = "Last Name", example = "Smith")
    private String lastName;

    @Schema(description = "User Email (acts as Username)", example = "alice@example.com")
    @Column(unique = true)
    private String email;

    @Schema(description = "BCrypt Encrypted Password", example = "$2a$10$...")
    private String password;

    @Schema(description = "User Role for Authorization", example = "ADMIN")
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Returns the authorities (permissions) granted to the user.
     * <p>
     * Spring Security uses this to decide if a user can access specific endpoints.
     * We convert our simple {@code Role} enum into a {@link SimpleGrantedAuthority}.
     * </p>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the password used to authenticate the user.
     * @return The encrypted password string.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * <p>
     * <b>Design Decision:</b> We use the {@code email} field as the unique username
     * for this application.
     * </p>
     */
    @Override
    public String getUsername() {
        return email;
    }

    // Boilerplate methods required by Spring Security.
    // In a complex app, these would check database columns (e.g., is_active).
    // For this MVP, we return 'true' to enable all users by default.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
