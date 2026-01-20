package com.example.library.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Aspect
@Component
public class LoggerAnswer {
    private final Logger log = LoggerFactory.getLogger(LoggerAnswer.class);

    @Pointcut("execution(* com.example.library..*(..)) && !within(com.example.library.jwt..*)")
    private void publicMethodsFromLoggingPackage() {
    }

    @Before("publicMethodsFromLoggingPackage()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder paramTypes = new StringBuilder("[");
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                paramTypes.append(arg != null ? arg.getClass().getSimpleName() : "null");
                if (i < args.length - 1) paramTypes.append(", ");
            }
        }
        paramTypes.append("]");

        log.info("---------------------->[BEFORE] {}.{} called with parameter types: {}", className, methodName, paramTypes);
    }

    @AfterReturning(value = "publicMethodsFromLoggingPackage()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String resultType = getResultTypeWithGeneric(result);

        log.info("---------------------->[AFTER] {}.{} completed, result type: {}", className, methodName, resultType);
    }

    @AfterThrowing(pointcut = "publicMethodsFromLoggingPackage()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("-------------------->[ERROR] Exception in {}.{}: {}", className, methodName, ex.getClass().getSimpleName());
    }

    private String getResultTypeWithGeneric(Object result) {
        if (result == null) {
            return "null";
        }

        if (result instanceof ResponseEntity<?>) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            Object body = response.getBody();
            String bodyType = (body != null) ? body.getClass().getSimpleName() : "null";
            return "ResponseEntity<" + bodyType + ">";
        }

        if (result instanceof Iterable<?>) {
            Iterable<?> iterable = (Iterable<?>) result;
            Iterator<?> it = iterable.iterator();
            String elementType = "";
            if (it.hasNext()) {
                elementType = it.next().getClass().getSimpleName();
            }
            return result.getClass().getSimpleName() + "<" + elementType + ">";
        }

        return result.getClass().getSimpleName();
    }
}