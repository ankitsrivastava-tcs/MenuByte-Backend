/**
 * Configuration class for web-related settings in the application.
 * This includes CORS mappings and interceptor registration.
 *
 * <p>
 * This class is responsible for configuring the application's web settings, such as enabling CORS for frontend communication
 * and registering interceptors for request processing.
 * </p>
 *
 * @author Ankit
 */
package com.menubyte.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS (Cross-Origin Resource Sharing) mappings.
     * This allows frontend applications running on different origins to communicate with the backend.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOriginPatterns(
            // 1. Production URL (your main, stable Vercel URL)
            "https://menu-byte-ui.vercel.app",

            // 2. Vercel Preview URL Pattern (handles all branch deployments)
            "https://menu-byte-*-ankitsrivastava-tcs-projects.vercel.app",
            
            // 3. Local Development URL
            "http://localhost:3000",
                "https://menubyte-ui.onrender.com",
                "http://192.168.29.137:3000"


        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
    }
}
