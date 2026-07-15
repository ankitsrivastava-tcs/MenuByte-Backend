package com.menubyte.exception;

import com.menubyte.dto.ApiErrorResponse;
import com.menubyte.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorLogService errorLogService;

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.CONFLICT, ex.getMessage(), request, false);
    }

    @ExceptionHandler(BusinessCountException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessCountException(BusinessCountException ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.CONFLICT, ex.getMessage(), request, false);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.BAD_REQUEST, "Invalid request.", request, false);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.CONFLICT, "The request conflicts with existing data.", request, true);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorResponse> handleSerialization(HttpMessageNotWritableException ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unable to prepare the response.", request, true);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return respond(ex, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, true);
    }

    private ResponseEntity<ApiErrorResponse> respond(Exception exception, HttpStatus status, String message,
                                                     HttpServletRequest request, boolean errorLevel) {
        if (errorLevel) {
            log.error("api_error status={} method={} path={}", status.value(), request.getMethod(), request.getRequestURI(), exception);
        } else {
            log.warn("api_error status={} method={} path={} exception={}", status.value(), request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
        }
        errorLogService.save(exception, status.value(), request);
        ApiErrorResponse body = new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), MDC.get("requestId"));
        return ResponseEntity.status(status).body(body);
    }
}
