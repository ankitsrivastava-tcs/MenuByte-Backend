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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    /**
     * Registers interceptors for request handling.
     * This method adds the {@link LoggingInterceptor} to the interceptor registry.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) mappings.
     * This allows frontend applications running on different origins to communicate with the backend.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow all endpoints
              //  .allowedOrigins("http://192.168.29.137:3000")// Allow frontend access
                .allowedOrigins("https://menu-byte-ui.vercel.app/") // Allow frontend access
               // .allowedOrigins("https://*.vercel.app") // Allow frontend access
//
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specified HTTP methods
                .allowedHeaders("*"); // Allow all headers
    }
}
