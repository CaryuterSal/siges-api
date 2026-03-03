package dev.spiffocode.sigesapi.common.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class SoftDeleteEnablerAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object enableFilterByDefault(ProceedingJoinPoint joinPoint) throws Throwable {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
        try {
            return joinPoint.proceed();
        } finally {
            session.disableFilter("softDeleteFilter");
        }
    }
}
