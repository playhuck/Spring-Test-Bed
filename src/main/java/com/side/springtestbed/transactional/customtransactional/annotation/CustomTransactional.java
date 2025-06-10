package com.side.springtestbed.transactional.customtransactional.annotation;

import java.lang.annotation.*;

/**
 * 커스텀 트랜잭션 어노테이션
 * - Spring @Transactional의 핵심 기능을 구현
 * - 메서드나 클래스 레벨에서 트랜잭션 메타데이터 정의
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomTransactional {
    
    /**
     * 트랜잭션 전파 정책
     * - REQUIRED: 기존 트랜잭션 참여 또는 새로 생성
     * - REQUIRES_NEW: 항상 새 트랜잭션 생성
     * - NESTED: Savepoint를 이용한 중첩 트랜잭션
     */
    Propagation propagation() default Propagation.REQUIRED;
    
    /**
     * 트랜잭션 격리 수준
     * - READ_COMMITTED: 커밋된 데이터만 읽기 (기본값)
     * - READ_UNCOMMITTED: 커밋되지 않은 데이터도 읽기
     * - REPEATABLE_READ: 반복 읽기 가능
     * - SERIALIZABLE: 직렬화 가능
     */
    Isolation isolation() default Isolation.READ_COMMITTED;
    
    /**
     * 읽기 전용 트랜잭션 여부
     * - true: 읽기 전용 최적화 적용
     * - false: 읽기/쓰기 모두 가능
     */
    boolean readOnly() default false;
    
    /**
     * 트랜잭션 타임아웃 (초 단위)
     * - 지정된 시간 내에 완료되지 않으면 롤백
     */
    int timeout() default 30;
    
    /**
     * 롤백을 발생시킬 예외 클래스들
     * - 지정된 예외 발생 시 트랜잭션 롤백
     */
    Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};
    
    /**
     * 롤백을 발생시키지 않을 예외 클래스들
     * - 지정된 예외 발생해도 트랜잭션 커밋
     */
    Class<? extends Throwable>[] noRollbackFor() default {};
}