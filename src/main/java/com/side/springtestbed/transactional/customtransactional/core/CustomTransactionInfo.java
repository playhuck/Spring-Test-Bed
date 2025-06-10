package com.side.springtestbed.transactional.customtransactional.core;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;

/**
 * 트랜잭션 세부 정보
 * - ThreadLocal에 저장되는 트랜잭션 컨텍스트 정보
 * - Spring TransactionInfo 역할
 */
@Getter
@Setter
public class CustomTransactionInfo {
    
    /**
     * 데이터베이스 연결
     * - 트랜잭션에서 사용하는 실제 DB Connection
     */
    private final Connection connection;
    
    /**
     * 트랜잭션 정의 정보
     * - 전파 정책, 격리 수준 등 트랜잭션 메타데이터
     */
    private final CustomTransactionDefinition definition;
    
    /**
     * 새로운 트랜잭션 여부
     * - true: 새로 생성된 트랜잭션
     * - false: 기존 트랜잭션에 참여하거나 중첩 트랜잭션
     */
    private final boolean newTransaction;
    
    /**
     * 트랜잭션 시작 시간
     * - 성능 모니터링 및 타임아웃 체크용
     */
    private final long startTime;
    
    /**
     * 이전 트랜잭션 정보 (스택 구조)
     * - REQUIRES_NEW나 NESTED에서 기존 트랜잭션 정보를 백업
     * - 트랜잭션 완료 후 복원용
     */
    private CustomTransactionInfo previousTransactionInfo;
    
    /**
     * Savepoint 이름 (중첩 트랜잭션용)
     * - NESTED 전파 정책에서 사용
     * - 부분 롤백 시 이 Savepoint로 롤백
     */
    private String savepointName;
    
    /**
     * 롤백 전용 마킹
     * - 참여 트랜잭션에서 예외 발생 시 true로 설정
     * - 외부 트랜잭션의 커밋을 방지
     */
    private boolean rollbackOnly = false;
    
    /**
     * 트랜잭션 완료 여부
     * - 커밋되었거나 롤백된 상태
     */
    private boolean completed = false;
    
    /**
     * 트랜잭션 활성 상태
     * - 현재 사용 중인 트랜잭션인지 여부
     */
    private boolean active = true;
    
    public CustomTransactionInfo(Connection connection, 
                                CustomTransactionDefinition definition, 
                                boolean newTransaction, 
                                long startTime) {
        this.connection = connection;
        this.definition = definition;
        this.newTransaction = newTransaction;
        this.startTime = startTime;
    }
    
    /**
     * 트랜잭션 실행 시간 계산
     * - 성능 모니터링용
     */
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 타임아웃 체크
     * - 설정된 타임아웃을 초과했는지 확인
     */
    public boolean isTimedOut() {
        if (definition.getTimeout() <= 0) {
            return false; // 타임아웃 설정 없음
        }
        
        long executionTimeSeconds = getExecutionTime() / 1000;
        return executionTimeSeconds > definition.getTimeout();
    }
    
    /**
     * 중첩 트랜잭션 여부 확인
     * - Savepoint가 설정되어 있으면 중첩 트랜잭션
     */
    public boolean isNested() {
        return savepointName != null;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CustomTransactionInfo{newTransaction=%s, rollbackOnly=%s, completed=%s, " +
            "active=%s, executionTime=%dms, nested=%s}",
            newTransaction, rollbackOnly, completed, active, getExecutionTime(), isNested()
        );
    }
}
