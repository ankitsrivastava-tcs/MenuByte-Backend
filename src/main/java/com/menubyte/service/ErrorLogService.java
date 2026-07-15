package com.menubyte.service;

import com.menubyte.entity.ErrorLog;
import com.menubyte.repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorLogService {
    private static final int MESSAGE_LIMIT = 4000;
    private static final int STACK_TRACE_LIMIT = 12000;

    private final ErrorLogRepository errorLogRepository;

    /** Saves independently so the error remains available even if the request transaction rolls back. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Exception exception, int statusCode, HttpServletRequest request) {
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setOccurredAt(LocalDateTime.now());
            errorLog.setRequestId(MDC.get("requestId"));
            errorLog.setHttpMethod(request.getMethod());
            errorLog.setRequestPath(request.getRequestURI());
            errorLog.setStatusCode(statusCode);
            errorLog.setExceptionType(exception.getClass().getName());
            errorLog.setMessage(truncate(exception.getMessage(), MESSAGE_LIMIT));
            errorLog.setStackTrace(truncate(stackTrace(exception), STACK_TRACE_LIMIT));
            errorLogRepository.save(errorLog);
        } catch (Exception persistenceException) {
            // Logging must never hide the original API error.
            log.error("Failed to persist error log for request {} {}", request.getMethod(), request.getRequestURI(), persistenceException);
        }
    }

    private String stackTrace(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private String truncate(String value, int limit) {
        if (value == null) {
            return null;
        }
        return value.length() <= limit ? value : value.substring(0, limit);
    }
}
