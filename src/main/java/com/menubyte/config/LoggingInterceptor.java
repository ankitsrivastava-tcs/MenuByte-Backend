package com.menubyte.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * LoggingInterceptor is a Spring component that implements the HandlerInterceptor interface.
 * It logs the HTTP response status after the request has been processed.
 *
 * <p>This interceptor ensures that the response writer is flushed to avoid potential buffering issues.</p>
 *
 * @author Ankit
 * @version 1.0
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    /**
     * This method is executed after the request has been completed.
     * It logs the response status and ensures the response writer is flushed.
     *
     * @param request  The HttpServletRequest object
     * @param response The HttpServletResponse object
     * @param handler  The handler (controller method) that processed the request
     * @param ex       Any exception thrown during request processing (can be null)
     * @throws Exception If an I/O error occurs while flushing the response writer
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("Response Status: " + response.getStatus());
        PrintWriter writer = response.getWriter();
        writer.flush(); // Ensure response is logged
    }
}
