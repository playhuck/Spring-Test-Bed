package com.side.springtestbed.transactional.propagation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring 기본 @Transactional을 사용한 전파 속성 데모 서비스
 * - Spring의 실제 트랜잭션 매니저와 @Transactional 사용
 * - 각 전파 정책의 실제 동작 방식 확인
 */
@Slf4j
@Service
public class SpringTransactionalDemoService {
    
    /**
     * REQUIRED 전파 정책 시나리오
     * - 기존 트랜잭션이 있으면 참여, 없으면 새로 생성
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredScenario() {
        logSpringTransactionState("REQUIRED 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 REQUIRED 작업");
        
        // 내부 REQUIRED 메서드 호출 (같은 트랜잭션 참여)
        requiredInnerMethod();
        
        logSpringTransactionState("REQUIRED 외부 트랜잭션", "종료");
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredInnerMethod() {
        logSpringTransactionState("REQUIRED 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("내부 REQUIRED 작업");
        
        logSpringTransactionState("REQUIRED 내부 트랜잭션", "종료");
    }
    
    /**
     * REQUIRES_NEW 전파 정책 시나리오
     * - 항상 새로운 트랜잭션 생성
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiresNewScenario() {
        logSpringTransactionState("REQUIRES_NEW 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 트랜잭션 작업");
        
        // 새로운 독립적인 트랜잭션 시작
        requiresNewInnerMethod();
        
        simulateBusinessLogic("외부 트랜잭션 계속");
        
        logSpringTransactionState("REQUIRES_NEW 외부 트랜잭션", "종료");
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewInnerMethod() {
        logSpringTransactionState("REQUIRES_NEW 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("독립적인 내부 트랜잭션 작업");
        
        logSpringTransactionState("REQUIRES_NEW 내부 트랜잭션", "종료");
    }
    
    /**
     * NESTED 전파 정책 시나리오
     * - Savepoint를 이용한 중첩 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void nestedScenario() {
        logSpringTransactionState("NESTED 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 트랜잭션 작업");
        
        // 중첩 트랜잭션 시작 (Savepoint 생성)
        nestedInnerMethod();
        
        simulateBusinessLogic("외부 트랜잭션 계속");
        
        logSpringTransactionState("NESTED 외부 트랜잭션", "종료");
    }
    
    @Transactional(propagation = Propagation.NESTED)
    public void nestedInnerMethod() {
        logSpringTransactionState("NESTED 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("중첩 트랜잭션 작업 (Savepoint)");
        
        logSpringTransactionState("NESTED 내부 트랜잭션", "종료");
    }
    
    /**
     * 예외 발생 시나리오 - REQUIRES_NEW
     * - 내부 트랜잭션 실패가 외부에 미치는 영향 확인
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiresNewWithExceptionScenario() {
        logSpringTransactionState("예외 시나리오 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 작업 - 성공할 예정");
        
        try {
            // 독립적인 트랜잭션에서 예외 발생
            requiresNewWithException();
        } catch (RuntimeException e) {
            log.error("[예외 처리] 내부 트랜잭션 실패했지만 외부는 계속: {}", e.getMessage());
        }
        
        simulateBusinessLogic("외부 작업 - 계속 진행");
        
        logSpringTransactionState("예외 시나리오 외부 트랜잭션", "종료");
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewWithException() {
        logSpringTransactionState("예외 발생 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("내부 작업 - 실패할 예정");
        
        throw new RuntimeException("REQUIRES_NEW 트랜잭션에서 의도적 예외 발생");
    }
    
    /**
     * 예외 발생 시나리오 - REQUIRED
     * - 같은 트랜잭션 내에서 예외 발생 시 전체 롤백
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredWithExceptionScenario() {
        logSpringTransactionState("REQUIRED 예외 시나리오 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 작업 - 성공");
        
        // 같은 트랜잭션에서 예외 발생 -> 전체 롤백
        requiredWithException();
        
        logSpringTransactionState("REQUIRED 예외 시나리오 외부 트랜잭션", "종료");
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredWithException() {
        logSpringTransactionState("REQUIRED 예외 발생 내부", "시작");
        
        simulateBusinessLogic("내부 작업 - 실패할 예정");
        
        throw new RuntimeException("REQUIRED 트랜잭션에서 의도적 예외 발생 - 전체 롤백됨");
    }
    
    /**
     * 복잡한 중첩 시나리오 (3단계)
     * - REQUIRED -> REQUIRES_NEW -> NESTED
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void complexNestedScenario() {
        logSpringTransactionState("복잡한 중첩 시나리오 Level 1", "시작");
        
        simulateBusinessLogic("Level 1 작업");
        
        // Level 2: 새로운 독립적 트랜잭션
        level2RequiresNew();
        
        simulateBusinessLogic("Level 1 작업 계속");
        
        logSpringTransactionState("복잡한 중첩 시나리오 Level 1", "종료");
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void level2RequiresNew() {
        logSpringTransactionState("복잡한 중첩 시나리오 Level 2", "시작");
        
        simulateBusinessLogic("Level 2 작업 (독립적)");
        
        // Level 3: 중첩 트랜잭션 (Savepoint)
        level3Nested();
        
        simulateBusinessLogic("Level 2 작업 계속");
        
        logSpringTransactionState("복잡한 중첩 시나리오 Level 2", "종료");
    }
    
    @Transactional(propagation = Propagation.NESTED)
    public void level3Nested() {
        logSpringTransactionState("복잡한 중첩 시나리오 Level 3", "시작");
        
        simulateBusinessLogic("Level 3 작업 (Savepoint)");
        
        logSpringTransactionState("복잡한 중첩 시나리오 Level 3", "종료");
    }
    
    /**
     * 다양한 전파 정책 조합 테스트
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void mixedPropagationScenario() {
        logSpringTransactionState("혼합 전파 정책 외부", "시작");
        
        // SUPPORTS - 기존 트랜잭션 참여
        supportsMethod();
        
        // NOT_SUPPORTED - 트랜잭션 일시 중단
        notSupportedMethod();
        
        // MANDATORY - 기존 트랜잭션 필수
        mandatoryMethod();
        
        logSpringTransactionState("혼합 전파 정책 외부", "종료");
    }
    
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsMethod() {
        logSpringTransactionState("SUPPORTS 메서드", "실행");
        simulateBusinessLogic("SUPPORTS 작업");
    }
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupportedMethod() {
        logSpringTransactionState("NOT_SUPPORTED 메서드", "실행");
        simulateBusinessLogic("NOT_SUPPORTED 작업");
    }
    
    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatoryMethod() {
        logSpringTransactionState("MANDATORY 메서드", "실행");
        simulateBusinessLogic("MANDATORY 작업");
    }
    
    /**
     * NEVER 전파 정책 테스트
     * - 트랜잭션이 있으면 예외 발생해야 함
     */
    @Transactional(propagation = Propagation.NEVER)
    public void neverMethod() {
        logSpringTransactionState("NEVER 메서드", "실행");
        simulateBusinessLogic("NEVER 작업");
    }
    
    /**
     * Spring 트랜잭션 상태 로깅
     * - TransactionSynchronizationManager를 사용한 상태 확인
     */
    private void logSpringTransactionState(String location, String phase) {
        Thread currentThread = Thread.currentThread();
        String threadInfo = String.format("스레드[ID:%d, Name:%s]", 
            currentThread.getId(), currentThread.getName());
        
        // Spring 트랜잭션 동기화 매니저 정보
        boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean isSynchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        
        log.info("\n" + "─".repeat(60));
        log.info("[{}] {} - {}", location, phase, threadInfo);
        log.info("[Spring 트랜잭션 상태]");
        log.info("  - 활성 트랜잭션: {}", isTransactionActive);
        log.info("  - 동기화 활성: {}", isSynchronizationActive);
        log.info("  - 트랜잭션 이름: {}", transactionName != null ? transactionName : "없음");
        log.info("  - 읽기 전용: {}", isReadOnly);
        log.info("  - 격리 수준: {}", isolationLevel != null ? getIsolationLevelName(isolationLevel) : "기본값");
        
        // 연결된 리소스 확인
        if (TransactionSynchronizationManager.hasResource(getClass())) {
            log.info("  - 바인딩된 리소스: 있음");
        } else {
            log.info("  - 바인딩된 리소스: 없음");
        }
        
        // 동기화 객체 수
        if (isSynchronizationActive) {
            log.info("  - 동기화 객체 수: {}", TransactionSynchronizationManager.getSynchronizations().size());
        }
        
        log.info("─".repeat(60));
    }
    
    /**
     * 격리 수준 이름 변환
     */
    private String getIsolationLevelName(int isolationLevel) {
        switch (isolationLevel) {
            case java.sql.Connection.TRANSACTION_NONE:
                return "NONE";
            case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
                return "READ_UNCOMMITTED";
            case java.sql.Connection.TRANSACTION_READ_COMMITTED:
                return "READ_COMMITTED";
            case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
                return "REPEATABLE_READ";
            case java.sql.Connection.TRANSACTION_SERIALIZABLE:
                return "SERIALIZABLE";
            default:
                return "UNKNOWN(" + isolationLevel + ")";
        }
    }
    
    /**
     * 비즈니스 로직 시뮬레이션
     */
    private void simulateBusinessLogic(String operation) {
        log.info("[비즈니스 로직] {} 실행 중...", operation);
        
        // 작업 시뮬레이션
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("[비즈니스 로직] {} 완료", operation);
    }
}
