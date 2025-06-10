package com.side.springtestbed.transactional.customtransactional.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 트랜잭션 상태 정보
 * - 현재 진행 중인 트랜잭션의 상태를 나타내는 객체
 * - Spring TransactionStatus 인터페이스 역할
 */
@Getter
@AllArgsConstructor
public class CustomTransactionStatus {
    
    /**
     * 트랜잭션 세부 정보
     * - Connection, Definition 등 트랜잭션 관련 모든 정보
     */
    private final CustomTransactionInfo transactionInfo;
    
    /**
     * 새로운 트랜잭션 여부
     * - true: 새로 생성된 트랜잭션 (실제 커밋/롤백 담당)
     * - false: 기존 트랜잭션에 참여 (커밋/롤백은 외부 트랜잭션이 담당)
     */
    private final boolean newTransaction;
    
    /**
     * 트랜잭션이 롤백 전용으로 마킹되었는지 확인
     * - 참여 트랜잭션에서 예외 발생 시 rollback-only로 마킹
     * - 외부 트랜잭션이 커밋을 시도해도 실제로는 롤백됨
     */
    public boolean isRollbackOnly() {
        return transactionInfo.isRollbackOnly();
    }
    
    /**
     * 트랜잭션이 완료되었는지 확인
     * - 커밋되었거나 롤백된 상태
     */
    public boolean isCompleted() {
        return transactionInfo.isCompleted();
    }
    
    /**
     * 현재 트랜잭션을 rollback-only로 마킹
     * - 참여 트랜잭션에서 예외 발생 시 사용
     * - 외부 트랜잭션의 커밋을 방지
     */
    public void setRollbackOnly() {
        transactionInfo.setRollbackOnly(true);
    }
    
    @Override
    public String toString() {
        return String.format(
            "CustomTransactionStatus{newTransaction=%s, rollbackOnly=%s, completed=%s}",
            newTransaction, isRollbackOnly(), isCompleted()
        );
    }
}
