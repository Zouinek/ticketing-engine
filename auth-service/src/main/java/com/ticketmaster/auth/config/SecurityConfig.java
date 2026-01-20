package com.ticketmaster.auth.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

/**
 * <h1>Main Security Configuration</h1>
 * <p>
 * This class serves as the central "Rule Book" for the application's security.
 * It ties together the authentication logic (Provider) and the custom filtering logic (JWT Filter).
 * </p>
 * <h2>Key Responsibilities:</h2>
 * <ul>
 * <li>Disabling unnecessary protections (like CSRF) for stateless APIs.</li>
 * <li>Defining which URLs are public (Whitelist) vs. protected.</li>
 * <li>Enforcing "Stateless" sessions (No server-side memory used for logins).</li>
 * <li>Injecting our custom JWT filter into the standard Spring Security chain.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configures the HTTP Security Filter Chain.
     * <p>
     * This method defines the exact sequence of security checks for every incoming request.
     * </p>
     * <h3>Configuration Steps:</h3>
     * <ol>
     * <li><b>CSRF Disable:</b> We disable Cross-Site Request Forgery protection because we are using stateless JWTs, not browser cookies.</li>
     * <li><b>Authorization Rules:</b>
     * <ul>
     * <li>{@code /api/v1/auth/**} -> Permitted (Login/Register)</li>
     * <li>Swagger UI paths -> Permitted (Documentation)</li>
     * <li>Any other request -> <b>Blocked</b> (Must be authenticated)</li>
     * </ul>
     * </li>
     * <li><b>Session Management:</b> Set to {@code STATELESS}. The server will not store any user session data in memory. Every request must be independently authenticated via Token.</li>
     * <li><b>Filter Placement:</b> We insert our {@code jwtAuthenticationFilter} <b>before</b> the standard Spring {@code UsernamePasswordAuthenticationFilter}. This ensures the Token is checked first.</li>
     * </ol>
     *
     * @param http The HttpSecurity object to configure.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API -> disable CSRF (otherwise POST/PUT/DELETE can be 403)
                .csrf(csrf -> csrf.disable())
                // Safe default; needed if a browser/frontend will call this API
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/system/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
