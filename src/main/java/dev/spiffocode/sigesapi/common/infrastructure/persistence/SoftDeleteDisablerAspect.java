package dev.spiffocode.sigesapi.common.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class SoftDeleteDisablerAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("@annotation(WithDeletedRecords)")
    public Object disableFilterForMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Session session = entityManager.unwrap(Session.class);
        log.info("Disabling SoftDeleteFilter");
        session.disableFilter("softDeleteFilter");
        try {
            return joinPoint.proceed();
        } finally {
            log.info("Re-enabling SoftDeleteFilter");
            session.enableFilter("softDeleteFilter");
        }
    }
}