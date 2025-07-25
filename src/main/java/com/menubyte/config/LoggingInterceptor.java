package com.menubyte.config; // Adjust package as necessary

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

@Component // Mark as a Spring component for dependency injection
@Slf4j // Lombok annotation for logging
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Wrap request and response to cache content for later logging
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        // Log request details
        log.info("Incoming Request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        log.debug("Request Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("  {}: {}", headerName, request.getHeader(headerName));
        }

        return true; // Continue processing the request
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // This method is called after the controller method but before view rendering.
        // Response body might not be fully available here if it's streamed.
        log.info("Request Processed: {} {} - Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // This method is called after the full request has been completed,
        // including view rendering, or after an exception.

        if (ex != null) {
            log.error("Request completed with exception for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.info("Request completed successfully for {} {} - Final Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        }

        // Log response body using the cached content
        if (response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
            byte[] content = responseWrapper.getContentAsByteArray();
            if (content.length > 0) {
                try {
                    String responseBody = new String(content, responseWrapper.getCharacterEncoding());
                    // Truncate long responses to avoid excessive logging
                    if (responseBody.length() > 1000) {
                        responseBody = responseBody.substring(0, 1000) + "... (truncated)";
                    }
                    log.debug("Response Body: {}", responseBody);
                } catch (UnsupportedEncodingException e) {
                    log.error("Error decoding response body: {}", e.getMessage());
                }
            }
            // IMPORTANT: Copy the cached content back to the original response
            // This is CRUCIAL for the client to receive the actual response body.
            responseWrapper.copyBodyToResponse();
        }
    }
}
