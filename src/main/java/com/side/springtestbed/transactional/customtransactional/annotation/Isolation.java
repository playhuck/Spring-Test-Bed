package com.side.springtestbed.transactional.customtransactional.annotation;

/**
 * 트랜잭션 격리 수준 정의
 * - 동시 실행되는 트랜잭션들 간의 격리 정도를 설정
 */
public enum Isolation {
    
    /**
     * 기본 격리 수준 (데이터베이스 기본값 사용)
     */
    DEFAULT,
    
    /**
     * 가장 낮은 격리 수준
     * - 다른 트랜잭션의 커밋되지 않은 데이터도 읽기 가능
     * - Dirty Read, Non-Repeatable Read, Phantom Read 모두 발생 가능
     */
    READ_UNCOMMITTED,
    
    /**
     * 일반적인 격리 수준
     * - 커밋된 데이터만 읽기 가능
     * - Dirty Read 방지, Non-Repeatable Read와 Phantom Read는 발생 가능
     */
    READ_COMMITTED,
    
    /**
     * 반복 읽기 가능한 격리 수준
     * - 같은 데이터를 여러 번 읽어도 동일한 결과 보장
     * - Dirty Read, Non-Repeatable Read 방지, Phantom Read는 발생 가능
     */
    REPEATABLE_READ,
    
    /**
     * 가장 높은 격리 수준
     * - 트랜잭션들이 순차적으로 실행되는 것과 동일한 결과 보장
     * - 모든 동시성 문제 방지 (성능 저하 발생)
     */
    SERIALIZABLE
}
