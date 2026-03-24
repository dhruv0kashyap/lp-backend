package com.linkedin.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.linkedin.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.debug("Entering {}.{}()", className, methodName);
        try {
            Object result = joinPoint.proceed();
            log.debug("Exiting {}.{}()", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("Exception in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        }
    }

    @AfterThrowing(pointcut = "execution(* com.linkedin.serviceImpl..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("Exception in {}.{}() with message: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}
