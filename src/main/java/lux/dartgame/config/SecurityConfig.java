package lux.dartgame.config;

import jakarta.servlet.http.HttpServletResponse;
import lux.dartgame.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Dart Game application.
 * Defines the {@link AuthenticationManager} and {@link SecurityFilterChain}
 * beans used to authenticate users and secure HTTP endpoints.
 */
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the security configuration with the required JWT filter.
     *
     * @param jwtAuthenticationFilterParam filter that validates JWT tokens
     */
    public SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilterParam) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilterParam;
    }

    /**
     * Builds the {@link SecurityFilterChain} that configures HTTP security.
     * Enables stateless session management, disables CSRF, permits
     * unauthenticated access to {@code /api/auth/**}, and registers the
     * {@link JwtAuthenticationFilter} before the default username/password
     * filter.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN))
                );

        return http.build();
    }

    /**
     * Creates and configures an {@link AuthenticationManager} backed by a
     * {@link DaoAuthenticationProvider}.
     *
     * @param userDetailsService the service used to load user-specific data
     * @param passwordEncoder    the encoder used to verify passwords
     * @return a fully configured {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final UserDetailsService userDetailsService,
            final PasswordEncoder passwordEncoder) {

        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
