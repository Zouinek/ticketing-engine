package com.ticketmaster.ticketing_engine.config;


import com.ticketmaster.ticketing_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <h1>Application Security Configuration</h1>
 * <p>
 * This class defines the "Beans" (Components) required for the authentication process.
 * It acts as the <b>Setup Factory</b> that wires together the Database, the Password Encoder,
 * and Spring Security.
 * </p>
 * <h2>Key Components Configured:</h2>
 * <ul>
 * <li><b>UserDetailsService:</b> How to find a user in our specific database.</li>
 * <li><b>PasswordEncoder:</b> How to encrypt/decrypt passwords (BCrypt).</li>
 * <li><b>AuthenticationProvider:</b> The logic that actually checks if a password is correct.</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Defines the logic for retrieving user details from the database.
     * <p>
     * When Spring Security needs to check a login, it calls this method.
     * We use a Lambda expression to simply search our {@code UserRepository} by email.
     * </p>
     *
     * @return A UserDetailsService implementation that fetches users by email.
     * @throws UsernameNotFoundException If the user with the given email does not exist.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    /**
     * Creates the Authentication Provider.
     * <p>
     * This is the <b>Data Access Object (DAO)</b> provider. It is responsible for two things:
     * <ol>
     * <li>Fetching user details (using the service defined above).</li>
     * <li>Encoding the password to see if it matches the stored hash.</li>
     * </ol>
     * </p>
     *
     * @return The configured provider ready to verify credentials.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * Exposes the AuthenticationManager bean.
     * <p>
     * The manager is the main entry point for authentication. When the {@code AuthenticationService}
     * needs to log a user in, it calls this manager.
     * </p>
     *
     * @param config The Spring Security configuration.
     * @return The standard AuthenticationManager.
     * @throws Exception If the configuration fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the password hashing algorithm.
     * <p>
     * We use <b>BCrypt</b>, which is the industry standard for secure password storage.
     * It adds "salt" to passwords so that even if two users have the same password,
     * their database entries will look different.
     * </p>
     *
     * @return A new BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
