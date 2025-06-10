package com.side.springtestbed.transactional.customtransactional.aop;

import com.side.springtestbed.transactional.customtransactional.annotation.CustomTransactional;
import com.side.springtestbed.transactional.customtransactional.core.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 커스텀 트랜잭션 AOP 인터셉터
 * - @CustomTransactional이 붙은 메서드를 가로채서 트랜잭션 처리
 * - Spring TransactionInterceptor 역할
 * - 실제 Spring @Transactional의 핵심 동작 구현
 */
@Slf4j
@Aspect
@Component
public class CustomTransactionalInterceptor {
    
    private final CustomTransactionManager transactionManager;
    
    @Autowired
    public CustomTransactionalInterceptor(CustomTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * @CustomTransactional 어노테이션이 붙은 메서드 가로채기
     * - 메서드 실행 전후로 트랜잭션 시작/커밋/롤백 처리
     * - AspectJ의 Around 어드바이스 사용
     */
    @Around("@annotation(customTransactional)")
    public Object intercept(ProceedingJoinPoint joinPoint, CustomTransactional customTransactional) 
            throws Throwable {
        
        // 메서드 정보 로깅
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("\n" + "=".repeat(80));
        log.info("[트랜잭션 인터셉터] 메서드 가로채기 시작: {}.{}", className, methodName);
        log.info("[트랜잭션 설정] {}", formatTransactionConfig(customTransactional));
        
        // 트랜잭션 정의 생성
        CustomTransactionDefinition definition = createTransactionDefinition(customTransactional);
        
        // 트랜잭션 시작
        CustomTransactionStatus status = null;
        boolean transactionActive = false;
        
        try {
            // 트랜잭션 매니저를 통해 트랜잭션 시작
            status = transactionManager.getTransaction(definition);
            transactionActive = true;
            
            log.info("[비즈니스 로직] 실제 메서드 실행 시작: {}.{}", className, methodName);
            
            // 실제 비즈니스 메서드 실행
            Object result = joinPoint.proceed();
            
            log.info("[비즈니스 로직] 실제 메서드 실행 완료: {}.{}", className, methodName);
            
            // 정상 완료 시 커밋
            transactionManager.commit(status);
            log.info("[트랜잭션 인터셉터] 트랜잭션 커밋 완료");
            
            return result;
            
        } catch (Throwable throwable) {
            
            // 예외 발생 시 롤백 여부 결정
            if (transactionActive && status != null) {
                if (definition.shouldRollbackFor(throwable)) {
                    log.error("[예외 처리] 롤백 대상 예외 발생: {}", throwable.getClass().getSimpleName());
                    log.error("[예외 메시지] {}", throwable.getMessage());
                    transactionManager.rollback(status);
                    log.info("[트랜잭션 인터셉터] 트랜잭션 롤백 완료");
                } else {
                    log.warn("[예외 처리] 롤백하지 않는 예외 발생: {}", throwable.getClass().getSimpleName());
                    log.warn("[예외 메시지] {}", throwable.getMessage());
                    transactionManager.commit(status);
                    log.info("[트랜잭션 인터셉터] 예외 발생했지만 트랜잭션 커밋 완료");
                }
            }
            
            // 예외 재발생 (호출자에게 전파)
            throw throwable;
            
        } finally {
            log.info("[트랜잭션 인터셉터] 메서드 가로채기 종료: {}.{}", className, methodName);
            log.info("=".repeat(80) + "\n");
        }
    }
    
    /**
     * @CustomTransactional 클래스 레벨 어노테이션 처리
     * - 클래스에 붙은 어노테이션은 모든 public 메서드에 적용
     */
    @Around("@within(customTransactional) && execution(public * *(..))")
    public Object interceptClass(ProceedingJoinPoint joinPoint, CustomTransactional customTransactional) 
            throws Throwable {
        
        // 메서드 레벨 어노테이션이 있는지 확인
        Method method = getMethod(joinPoint);
        CustomTransactional methodAnnotation = method.getAnnotation(CustomTransactional.class);
        
        // 메서드 레벨 어노테이션이 우선순위가 높음
        CustomTransactional effectiveAnnotation = methodAnnotation != null ? methodAnnotation : customTransactional;
        
        return intercept(joinPoint, effectiveAnnotation);
    }
    
    /**
     * 어노테이션 정보로부터 트랜잭션 정의 객체 생성
     * - 전파 정책, 격리 수준, 타임아웃 등 모든 설정 포함
     */
    private CustomTransactionDefinition createTransactionDefinition(CustomTransactional annotation) {
        return new CustomTransactionDefinition(
            annotation.propagation(),
            annotation.isolation(),
            annotation.readOnly(),
            annotation.timeout(),
            annotation.rollbackFor(),
            annotation.noRollbackFor()
        );
    }
    
    /**
     * ProceedingJoinPoint에서 실제 Method 객체 추출
     * - 리플렉션을 통해 메서드 정보 획득
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature())
                    .getParameterTypes();
            
            return joinPoint.getTarget().getClass().getMethod(methodName, parameterTypes);
            
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("메서드 정보 추출 실패", e);
        }
    }
    
    /**
     * 트랜잭션 설정 정보를 보기 좋게 포맷팅
     * - 로깅용 헬퍼 메서드
     */
    private String formatTransactionConfig(CustomTransactional annotation) {
        return String.format(
            "propagation=%s, isolation=%s, readOnly=%s, timeout=%ds",
            annotation.propagation(),
            annotation.isolation(),
            annotation.readOnly(),
            annotation.timeout()
        );
    }
}
