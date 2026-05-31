package com.example.spiceshub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") 
                        // ✅ FIX: Changed allowedOrigins to allowedOriginPatterns to process dynamic local ports properly
                        .allowedOriginPatterns(
                            "http://localhost:[*]", 
                            "https://spicehubadmin.netlify.app"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true) 
                        .maxAge(3600);
            }
        };
    }
}