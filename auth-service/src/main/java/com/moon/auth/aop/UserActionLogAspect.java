package com.moon.auth.aop;

import com.moon.auth.annotation.UserActionLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class UserActionLogAspect {

    @Around("@annotation(userActionLog)")
    public Object logUserAction(ProceedingJoinPoint joinPoint, UserActionLog userActionLog) throws Throwable {
        String action = userActionLog.action().isBlank() ? userActionLog.value() : userActionLog.action();
        String method = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            log.info("user_action action={} method={} success=true", action, method);
            return result;
        } catch (Throwable throwable) {
            log.warn("user_action action={} method={} success=false reason={}", action, method, throwable.getMessage());
            throw throwable;
        }
    }
}
