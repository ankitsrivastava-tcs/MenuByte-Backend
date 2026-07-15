package com.menubyte.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Persistent record of a failed API request. Never store credentials, payment
 * signatures, request bodies, or other sensitive values in this table.
 */
@Entity
@Table(name = "error_logs", indexes = {
        @Index(name = "idx_error_logs_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_error_logs_request_id", columnList = "request_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_path", length = 512)
    private String requestPath;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "exception_type", length = 512)
    private String exceptionType;

    @Column(length = 4000)
    private String message;

    @Column(name = "stack_trace", length = 12000)
    private String stackTrace;
}
