package com.side.springtestbed.transactional.propagation;

import com.side.springtestbed.transactional.customtransactional.annotation.CustomTransactional;
import com.side.springtestbed.transactional.customtransactional.annotation.Propagation;
import com.side.springtestbed.transactional.customtransactional.core.CustomTransactionSynchronizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 전파 속성별 트랜잭션 동작 데모 서비스
 * - 각 전파 정책의 동작 방식을 실제로 확인
 * - 스레드와 트랜잭션 상태 변화를 로깅으로 추적
 */
@Slf4j
@Service
public class PropagationDemoService {
    
    /**
     * REQUIRED 전파 정책 시나리오
     * - 기존 트랜잭션이 있으면 참여, 없으면 새로 생성
     * - 가장 일반적인 전파 정책
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiredScenario() {
        logTransactionState("REQUIRED 외부 트랜잭션", "시작");
        
        // 비즈니스 로직 시뮬레이션
        simulateBusinessLogic("외부 REQUIRED 작업");
        
        // 내부 REQUIRED 메서드 호출 (같은 트랜잭션 참여)
        requiredInnerMethod();
        
        logTransactionState("REQUIRED 외부 트랜잭션", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiredInnerMethod() {
        logTransactionState("REQUIRED 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("내부 REQUIRED 작업");
        
        logTransactionState("REQUIRED 내부 트랜잭션", "종료");
    }
    
    /**
     * REQUIRES_NEW 전파 정책 시나리오
     * - 항상 새로운 트랜잭션 생성
     * - 기존 트랜잭션과 완전히 독립적
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiresNewScenario() {
        logTransactionState("REQUIRES_NEW 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 트랜잭션 작업");
        
        // 새로운 독립적인 트랜잭션 시작
        requiresNewInnerMethod();
        
        // 외부 트랜잭션으로 복귀
        simulateBusinessLogic("외부 트랜잭션 계속");
        
        logTransactionState("REQUIRES_NEW 외부 트랜잭션", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewInnerMethod() {
        logTransactionState("REQUIRES_NEW 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("독립적인 내부 트랜잭션 작업");
        
        logTransactionState("REQUIRES_NEW 내부 트랜잭션", "종료");
    }
    
    /**
     * NESTED 전파 정책 시나리오
     * - Savepoint를 이용한 중첩 트랜잭션
     * - 부분 롤백 가능
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void nestedScenario() {
        logTransactionState("NESTED 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 트랜잭션 작업");
        
        // 중첩 트랜잭션 시작 (Savepoint 생성)
        nestedInnerMethod();
        
        // 외부 트랜잭션 계속
        simulateBusinessLogic("외부 트랜잭션 계속");
        
        logTransactionState("NESTED 외부 트랜잭션", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.NESTED)
    public void nestedInnerMethod() {
        logTransactionState("NESTED 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("중첩 트랜잭션 작업 (Savepoint)");
        
        logTransactionState("NESTED 내부 트랜잭션", "종료");
    }
    
    /**
     * 예외 발생 시나리오 - REQUIRES_NEW
     * - 내부 트랜잭션 실패가 외부에 미치는 영향 확인
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiresNewWithExceptionScenario() {
        logTransactionState("예외 시나리오 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 작업 - 성공할 예정");
        
        try {
            // 독립적인 트랜잭션에서 예외 발생
            requiresNewWithException();
        } catch (RuntimeException e) {
            log.error("[예외 처리] 내부 트랜잭션 실패했지만 외부는 계속: {}", e.getMessage());
        }
        
        simulateBusinessLogic("외부 작업 - 계속 진행");
        
        logTransactionState("예외 시나리오 외부 트랜잭션", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewWithException() {
        logTransactionState("예외 발생 내부 트랜잭션", "시작");
        
        simulateBusinessLogic("내부 작업 - 실패할 예정");
        
        throw new RuntimeException("REQUIRES_NEW 트랜잭션에서 의도적 예외 발생");
    }
    
    /**
     * 예외 발생 시나리오 - REQUIRED
     * - 같은 트랜잭션 내에서 예외 발생 시 전체 롤백
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiredWithExceptionScenario() {
        logTransactionState("REQUIRED 예외 시나리오 외부 트랜잭션", "시작");
        
        simulateBusinessLogic("외부 작업 - 성공");
        
        // 같은 트랜잭션에서 예외 발생 -> 전체 롤백
        requiredWithException();
        
        logTransactionState("REQUIRED 예외 시나리오 외부 트랜잭션", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void requiredWithException() {
        logTransactionState("REQUIRED 예외 발생 내부", "시작");
        
        simulateBusinessLogic("내부 작업 - 실패할 예정");
        
        throw new RuntimeException("REQUIRED 트랜잭션에서 의도적 예외 발생 - 전체 롤백됨");
    }
    
    /**
     * 복잡한 중첩 시나리오
     * - 3단계 중첩: REQUIRED -> REQUIRES_NEW -> NESTED
     */
    @CustomTransactional(propagation = Propagation.REQUIRED)
    public void complexNestedScenario() {
        logTransactionState("복잡한 중첩 시나리오 Level 1", "시작");
        
        simulateBusinessLogic("Level 1 작업");
        
        // Level 2: 새로운 독립적 트랜잭션
        level2RequiresNew();
        
        simulateBusinessLogic("Level 1 작업 계속");
        
        logTransactionState("복잡한 중첩 시나리오 Level 1", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.REQUIRES_NEW)
    public void level2RequiresNew() {
        logTransactionState("복잡한 중첩 시나리오 Level 2", "시작");
        
        simulateBusinessLogic("Level 2 작업 (독립적)");
        
        // Level 3: 중첩 트랜잭션 (Savepoint)
        level3Nested();
        
        simulateBusinessLogic("Level 2 작업 계속");
        
        logTransactionState("복잡한 중첩 시나리오 Level 2", "종료");
    }
    
    @CustomTransactional(propagation = Propagation.NESTED)
    public void level3Nested() {
        logTransactionState("복잡한 중첩 시나리오 Level 3", "시작");
        
        simulateBusinessLogic("Level 3 작업 (Savepoint)");
        
        logTransactionState("복잡한 중첩 시나리오 Level 3", "종료");
    }
    
    /**
     * 트랜잭션 상태와 스레드 정보 로깅
     * - 현재 트랜잭션 상태를 상세히 출력
     * - ThreadLocal 스택 정보 포함
     */
    private void logTransactionState(String location, String phase) {
        Thread currentThread = Thread.currentThread();
        String threadInfo = String.format("스레드[ID:%d, Name:%s]", 
            currentThread.getId(), currentThread.getName());
        
        boolean hasTransaction = CustomTransactionSynchronizationManager.hasCurrentTransaction();
        int stackDepth = CustomTransactionSynchronizationManager.getTransactionStackDepth();
        
        log.info("\n" + "─".repeat(60));
        log.info("[{}] {} - {}", location, phase, threadInfo);
        log.info("[트랜잭션 상태] 활성 트랜잭션: {}, 스택 깊이: {}", hasTransaction, stackDepth);
        
        if (hasTransaction) {
            String debugInfo = CustomTransactionSynchronizationManager.getDebugInfo();
            log.info("[ThreadLocal 상태]\n{}", debugInfo);
        } else {
            log.info("[ThreadLocal 상태] 활성 트랜잭션 없음");
        }
        
        log.info("─".repeat(60));
    }
    
    /**
     * 비즈니스 로직 시뮬레이션
     * - 실제 작업을 시뮬레이션하는 더미 메서드
     */
    private void simulateBusinessLogic(String operation) {
        log.info("[비즈니스 로직] {} 실행 중...", operation);
        
        // 작업 시뮬레이션 (짧은 대기)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("[비즈니스 로직] {} 완료", operation);
    }
}
