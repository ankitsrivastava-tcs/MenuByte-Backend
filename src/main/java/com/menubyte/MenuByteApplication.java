/**
 * Entry point for the MenuByte application.
 * Bootstraps the Spring Boot application.
 *
 * @author Ankit Srivastava
 */
package com.menubyte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class MenuByteApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application
        SpringApplication.run(MenuByteApplication.class, args);

        // Logging a welcome message to confirm the app is running
        log.info("MenuByte application is running! Enjoy Digital Menu!!");
    }
}