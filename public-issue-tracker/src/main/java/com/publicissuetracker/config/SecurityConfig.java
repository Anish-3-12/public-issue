package com.publicissuetracker.config;

import com.publicissuetracker.repository.UserRepository;
import com.publicissuetracker.security.JwtAuthenticationFilter;
import com.publicissuetracker.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Register the JWT filter bean so we can inject dependencies in the filter.
     * Note: we return the filter instance (not annotated as @Component).
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        return new JwtAuthenticationFilter(jwtUtil, userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // enable CORS so your CorsConfig bean is picked up (new style for Spring Security 6.1+)
                .cors(Customizer.withDefaults())
                // disable CSRF for a stateless REST API
                .csrf(csrf -> csrf.disable())
                // stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // authorization rules
                .authorizeHttpRequests(auth -> auth
                        // allow unauthenticated access to auth endpoints, actuator and H2 console
                        .requestMatchers("/api/v1/auth/**", "/actuator/**", "/h2-console/**").permitAll()
                        // allow preflight OPTIONS requests from the browser
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // all other requests require authentication
                        .anyRequest().authenticated()
                )
                // disable form login
                .formLogin(form -> form.disable())
                // disable HTTP Basic (avoid browser username/password popup)
                .httpBasic(httpBasic -> httpBasic.disable());

        // allow frames for H2 console in dev
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}




