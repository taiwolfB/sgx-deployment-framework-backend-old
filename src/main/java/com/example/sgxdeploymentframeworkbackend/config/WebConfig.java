package com.example.sgxdeploymentframeworkbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(environment.getProperty("frontend.server"))
                .allowedMethods("GET", "PUT", "POST", "DELETE")
                .allowedHeaders("Accept", "Content-Type")
                .exposedHeaders("Accept", "Content-Type")
                .allowCredentials(false).maxAge(3600);
    }
}
