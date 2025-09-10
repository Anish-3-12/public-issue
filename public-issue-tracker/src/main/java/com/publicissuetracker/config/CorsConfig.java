package com.publicissuetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // allow only your frontend origin in dev
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // allow common HTTP methods
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        // explicitly allow Authorization header (and content-type)
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));

        // allow the browser to read the Authorization header in the response if needed
        config.setExposedHeaders(List.of("Authorization"));

        // allow cookies/credentials if you ever use them
        config.setAllowCredentials(true);

        // cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


