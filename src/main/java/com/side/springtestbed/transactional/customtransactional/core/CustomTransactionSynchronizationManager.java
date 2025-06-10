package com.side.springtestbed.transactional.customtransactional.core;

import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 동기화 매니저
 * - ThreadLocal을 이용한 트랜잭션 정보 관리
 * - Spring TransactionSynchronizationManager 역할
 * - 스레드별 독립적인 트랜잭션 컨텍스트 제공
 */
@Slf4j
public class CustomTransactionSynchronizationManager {
    
    /**
     * 스레드별 트랜잭션 정보 저장소
     * - 각 스레드마다 독립적인 트랜잭션 컨텍스트 유지
     * - 중첩 트랜잭션 시 스택 구조로 이전 정보 백업/복원
     */
    private static final ThreadLocal<CustomTransactionInfo> transactionInfoThreadLocal = 
            new ThreadLocal<>();
    
    /**
     * 현재 스레드의 트랜잭션 정보 조회
     * - 활성화된 트랜잭션이 있으면 해당 정보 반환
     * - 없으면 null 반환
     */
    public static CustomTransactionInfo getCurrentTransactionInfo() {
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        if (current != null) {
            log.debug("[ThreadLocal 조회] 현재 트랜잭션: {}", current);
        } else {
            log.debug("[ThreadLocal 조회] 활성 트랜잭션 없음");
        }
        return current;
    }
    
    /**
     * 트랜잭션 정보 바인딩
     * - 새로운 트랜잭션 정보를 ThreadLocal에 설정
     * - 기존 트랜잭션이 있으면 새 트랜잭션의 이전 정보로 백업
     * - 중첩 트랜잭션에서 스택 구조 구현
     */
    public static void bindTransactionInfo(CustomTransactionInfo transactionInfo) {
        // 기존 트랜잭션 정보 백업
        CustomTransactionInfo previous = transactionInfoThreadLocal.get();
        if (previous != null) {
            transactionInfo.setPreviousTransactionInfo(previous);
            log.info("[ThreadLocal 바인딩] 기존 트랜잭션 백업하고 새 트랜잭션 설정");
            log.debug("  - 이전 트랜잭션: {}", previous);
            log.debug("  - 새 트랜잭션: {}", transactionInfo);
        } else {
            log.info("[ThreadLocal 바인딩] 새 트랜잭션 설정 (최초)");
            log.debug("  - 새 트랜잭션: {}", transactionInfo);
        }
        
        // 새 트랜잭션 정보 설정
        transactionInfoThreadLocal.set(transactionInfo);
    }
    
    /**
     * 현재 트랜잭션 일시 중단
     * - REQUIRES_NEW 전파 정책에서 사용
     * - 현재 트랜잭션을 백업하고 ThreadLocal에서 제거
     * - 새 트랜잭션 완료 후 복원 가능하도록 정보 반환
     */
    public static CustomTransactionInfo suspendCurrentTransaction() {
        CustomTransactionInfo suspended = transactionInfoThreadLocal.get();
        if (suspended != null) {
            suspended.setActive(false);
            transactionInfoThreadLocal.remove();
            log.info("[ThreadLocal 중단] 트랜잭션 일시 중단");
            log.debug("  - 중단된 트랜잭션: {}", suspended);
        } else {
            log.debug("[ThreadLocal 중단] 중단할 트랜잭션 없음");
        }
        return suspended;
    }
    
    /**
     * 중단된 트랜잭션 복원
     * - suspendCurrentTransaction()으로 중단된 트랜잭션을 복원
     * - REQUIRES_NEW 완료 후 외부 트랜잭션 재개 시 사용
     */
    public static void resumeTransaction(CustomTransactionInfo suspendedTransaction) {
        if (suspendedTransaction != null) {
            suspendedTransaction.setActive(true);
            transactionInfoThreadLocal.set(suspendedTransaction);
            log.info("[ThreadLocal 복원] 중단된 트랜잭션 복원");
            log.debug("  - 복원된 트랜잭션: {}", suspendedTransaction);
        } else {
            log.debug("[ThreadLocal 복원] 복원할 트랜잭션 없음");
        }
    }
    
    /**
     * 트랜잭션 정보 정리 및 이전 트랜잭션 복원
     * - 현재 트랜잭션 완료 후 호출
     * - 이전 트랜잭션이 있으면 복원, 없으면 ThreadLocal 제거
     * - 중첩 트랜잭션의 스택 구조에서 이전 레벨로 복귀
     */
    public static void cleanupTransactionInfo() {
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        if (current != null) {
            // 현재 트랜잭션 완료 처리
            current.setCompleted(true);
            current.setActive(false);
            
            // 이전 트랜잭션 복원
            CustomTransactionInfo previous = current.getPreviousTransactionInfo();
            if (previous != null) {
                transactionInfoThreadLocal.set(previous);
                log.info("[ThreadLocal 정리] 이전 트랜잭션으로 복원");
                log.debug("  - 완료된 트랜잭션: {}", current);
                log.debug("  - 복원된 트랜잭션: {}", previous);
            } else {
                transactionInfoThreadLocal.remove();
                log.info("[ThreadLocal 정리] ThreadLocal 완전 제거");
                log.debug("  - 완료된 트랜잭션: {}", current);
            }
        } else {
            log.debug("[ThreadLocal 정리] 정리할 트랜잭션 없음");
        }
    }
    
    /**
     * ThreadLocal 강제 초기화
     * - 예외 상황이나 테스트에서 ThreadLocal 상태 완전 초기화
     * - 메모리 누수 방지용
     */
    public static void clear() {
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        if (current != null) {
            log.warn("[ThreadLocal 강제 초기화] 활성 트랜잭션이 있지만 강제 정리");
            log.debug("  - 정리된 트랜잭션: {}", current);
        }
        transactionInfoThreadLocal.remove();
    }
    
    /**
     * 현재 트랜잭션 존재 여부 확인
     * - 활성화된 트랜잭션이 있는지 간단히 체크
     */
    public static boolean hasCurrentTransaction() {
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        return current != null && current.isActive();
    }
    
    /**
     * 트랜잭션 스택 깊이 조회
     * - 중첩된 트랜잭션의 깊이 확인
     * - 디버깅 및 모니터링용
     */
    public static int getTransactionStackDepth() {
        int depth = 0;
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        
        while (current != null) {
            depth++;
            current = current.getPreviousTransactionInfo();
        }
        
        return depth;
    }
    
    /**
     * 현재 스레드의 트랜잭션 상태 디버그 정보
     * - 개발 및 디버깅 시 트랜잭션 상태 확인용
     */
    public static String getDebugInfo() {
        CustomTransactionInfo current = transactionInfoThreadLocal.get();
        if (current == null) {
            return "No active transaction";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction Stack (depth: ").append(getTransactionStackDepth()).append("):\n");
        
        int level = 1;
        CustomTransactionInfo info = current;
        while (info != null) {
            sb.append("  Level ").append(level++).append(": ").append(info).append("\n");
            info = info.getPreviousTransactionInfo();
        }
        
        return sb.toString();
    }
}
