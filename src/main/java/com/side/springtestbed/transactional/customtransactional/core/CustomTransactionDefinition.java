package com.side.springtestbed.transactional.customtransactional.core;

import com.side.springtestbed.transactional.customtransactional.annotation.Isolation;
import com.side.springtestbed.transactional.customtransactional.annotation.Propagation;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 트랜잭션 정의 정보
 * - @CustomTransactional 어노테이션에서 추출한 메타데이터를 담는 객체
 * - Spring TransactionDefinition 인터페이스 역할
 */
@Getter
@AllArgsConstructor
public class CustomTransactionDefinition {
    
    /**
     * 트랜잭션 전파 정책
     * - 트랜잭션이 시작되거나 참여하는 방식
     */
    private final Propagation propagation;
    
    /**
     * 트랜잭션 격리 수준
     * - 동시 실행되는 트랜잭션들 간의 격리 정도
     */
    private final Isolation isolation;
    
    /**
     * 읽기 전용 트랜잭션 여부
     * - true인 경우 최적화 적용 가능
     */
    private final boolean readOnly;
    
    /**
     * 트랜잭션 타임아웃 (초 단위)
     * - 지정된 시간 내에 완료되지 않으면 롤백
     */
    private final int timeout;
    
    /**
     * 롤백을 발생시킬 예외 클래스들
     * - 이 예외들이 발생하면 트랜잭션 롤백
     */
    private final Class<? extends Throwable>[] rollbackFor;
    
    /**
     * 롤백을 발생시키지 않을 예외 클래스들
     * - 이 예외들이 발생해도 트랜잭션 커밋
     */
    private final Class<? extends Throwable>[] noRollbackFor;
    
    /**
     * 예외가 롤백 대상인지 판단
     * - rollbackFor와 noRollbackFor 설정을 기반으로 결정
     * - RuntimeException은 기본적으로 롤백 대상
     */
    public boolean shouldRollbackFor(Throwable throwable) {
        // noRollbackFor에 포함된 예외는 롤백하지 않음
        for (Class<? extends Throwable> noRollbackClass : noRollbackFor) {
            if (noRollbackClass.isAssignableFrom(throwable.getClass())) {
                return false;
            }
        }
        
        // rollbackFor에 포함된 예외는 롤백
        for (Class<? extends Throwable> rollbackClass : rollbackFor) {
            if (rollbackClass.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        
        // 기본 규칙: RuntimeException과 Error는 롤백, Checked Exception은 롤백하지 않음
        return throwable instanceof RuntimeException || throwable instanceof Error;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CustomTransactionDefinition{propagation=%s, isolation=%s, readOnly=%s, timeout=%d}",
            propagation, isolation, readOnly, timeout
        );
    }
}
