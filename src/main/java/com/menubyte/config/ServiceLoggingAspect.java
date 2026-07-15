package com.menubyte.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** Logs service failures consistently without logging method arguments or return values. */
@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("execution(public * com.menubyte.service..*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        long startedAt = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.debug("service_complete operation={} durationMs={}", operation, System.currentTimeMillis() - startedAt);
            return result;
        } catch (Throwable exception) {
            log.error("service_failed operation={} durationMs={}", operation, System.currentTimeMillis() - startedAt, exception);
            throw exception;
        }
    }
}
