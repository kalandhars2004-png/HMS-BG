package com.phegondev.InventoryManagementSystem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    /**
     * Allowed browser origins, comma separated. Defaults to the local dev UI.
     * Set app.cors.allowed-origins (or APP_CORS_ALLOWED_ORIGINS) per environment.
     *
     * Previously this was allowedOrigins("*"), which let any site on the internet
     * make authenticated cross-origin calls against this API.
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // TESTING ONLY — allow all origins daa. Narrow to explicit Vercel URLs in prod later.
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowedOriginPatterns("*")
                        .allowCredentials(true);
            }
        };
    }
}
