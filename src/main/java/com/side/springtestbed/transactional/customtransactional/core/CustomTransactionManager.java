package com.side.springtestbed.transactional.customtransactional.core;

import com.side.springtestbed.transactional.customtransactional.annotation.Isolation;
import com.side.springtestbed.transactional.customtransactional.annotation.Propagation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 커스텀 트랜잭션 매니저
 * - 실제 트랜잭션 시작, 커밋, 롤백을 담당
 * - Spring PlatformTransactionManager 역할
 */
@Slf4j
@Component
public class CustomTransactionManager {
    
    private final DataSource dataSource;
    
    public CustomTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 트랜잭션 시작
     * - 전파 정책에 따라 새 트랜잭션 생성 또는 기존 트랜잭션 참여 결정
     * - Connection 획득 및 트랜잭션 설정 적용
     */
    public CustomTransactionStatus getTransaction(CustomTransactionDefinition definition) {
        log.info("[트랜잭션 매니저] 트랜잭션 시작 요청 - 전파정책: {}", definition.getPropagation());
        
        // 현재 스레드의 기존 트랜잭션 정보 확인
        CustomTransactionInfo currentTransaction = CustomTransactionSynchronizationManager.getCurrentTransactionInfo();
        
        // 전파 정책에 따른 트랜잭션 처리
        return handlePropagation(definition, currentTransaction);
    }
    
    /**
     * 전파 정책별 트랜잭션 처리 로직
     * - REQUIRED: 기존 트랜잭션 참여 또는 새로 생성
     * - REQUIRES_NEW: 기존 트랜잭션 중단 후 새로 생성
     * - NESTED: Savepoint를 이용한 중첩 트랜잭션
     */
    private CustomTransactionStatus handlePropagation(CustomTransactionDefinition definition, 
                                                     CustomTransactionInfo currentTransaction) {
        
        Propagation propagation = definition.getPropagation();
        
        switch (propagation) {
            case REQUIRED:
                if (currentTransaction != null && currentTransaction.isActive()) {
                    log.info("[전파정책-REQUIRED] 기존 트랜잭션 참여");
                    return new CustomTransactionStatus(currentTransaction, false);
                } else {
                    log.info("[전파정책-REQUIRED] 새 트랜잭션 생성");
                    return createNewTransaction(definition);
                }
                
            case REQUIRES_NEW:
                log.info("[전파정책-REQUIRES_NEW] 새 트랜잭션 생성 (기존 트랜잭션 중단)");
                if (currentTransaction != null) {
                    suspendCurrentTransaction(currentTransaction);
                }
                return createNewTransaction(definition);
                
            case NESTED:
                if (currentTransaction != null && currentTransaction.isActive()) {
                    log.info("[전파정책-NESTED] 중첩 트랜잭션 생성 (Savepoint)");
                    return createNestedTransaction(currentTransaction, definition);
                } else {
                    log.info("[전파정책-NESTED] 기존 트랜잭션 없음, 새 트랜잭션 생성");
                    return createNewTransaction(definition);
                }
                
            default:
                throw new IllegalArgumentException("지원하지 않는 전파 정책: " + propagation);
        }
    }
    
    /**
     * 새로운 트랜잭션 생성
     * - DataSource에서 Connection 획득
     * - 격리 수준, 읽기 전용 등 트랜잭션 속성 설정
     * - ThreadLocal에 트랜잭션 정보 바인딩
     */
    private CustomTransactionStatus createNewTransaction(CustomTransactionDefinition definition) {
        try {
            // Connection 획득
            Connection connection = dataSource.getConnection();
            log.info("[트랜잭션 생성] Connection 획득: {}", connection.hashCode());
            
            // 트랜잭션 설정 적용
            setupTransactionConnection(connection, definition);
            
            // 트랜잭션 정보 생성 및 ThreadLocal 바인딩
            CustomTransactionInfo transactionInfo = new CustomTransactionInfo(
                connection, definition, true, System.currentTimeMillis()
            );
            
            CustomTransactionSynchronizationManager.bindTransactionInfo(transactionInfo);
            
            return new CustomTransactionStatus(transactionInfo, true);
            
        } catch (SQLException e) {
            log.error("[트랜잭션 생성 실패] {}", e.getMessage());
            throw new RuntimeException("트랜잭션 생성 실패", e);
        }
    }
    
    /**
     * Connection에 트랜잭션 설정 적용
     * - 자동 커밋 비활성화
     * - 격리 수준 설정
     * - 읽기 전용 설정
     */
    private void setupTransactionConnection(Connection connection, CustomTransactionDefinition definition) 
            throws SQLException {
        
        // 자동 커밋 비활성화 (트랜잭션 모드)
        connection.setAutoCommit(false);
        log.info("[Connection 설정] AutoCommit=false");
        
        // 격리 수준 설정
        if (definition.getIsolation() != Isolation.DEFAULT) {
            int isolationLevel = convertIsolationLevel(definition.getIsolation());
            connection.setTransactionIsolation(isolationLevel);
            log.info("[Connection 설정] 격리수준: {}", definition.getIsolation());
        }
        
        // 읽기 전용 설정
        if (definition.isReadOnly()) {
            connection.setReadOnly(true);
            log.info("[Connection 설정] ReadOnly=true");
        }
    }
    
    /**
     * 중첩 트랜잭션 생성 (Savepoint 사용)
     * - 기존 Connection에서 Savepoint 생성
     * - 부분 롤백 가능한 중첩 트랜잭션 구조
     */
    private CustomTransactionStatus createNestedTransaction(CustomTransactionInfo currentTransaction, 
                                                           CustomTransactionDefinition definition) {
        try {
            Connection connection = currentTransaction.getConnection();
            
            // Savepoint 생성
            String savepointName = "SAVEPOINT_" + System.currentTimeMillis();
            connection.setSavepoint(savepointName);
            log.info("[중첩 트랜잭션] Savepoint 생성: {}", savepointName);
            
            // 중첩 트랜잭션 정보 생성
            CustomTransactionInfo nestedInfo = new CustomTransactionInfo(
                connection, definition, false, System.currentTimeMillis()
            );
            nestedInfo.setSavepointName(savepointName);
            
            CustomTransactionSynchronizationManager.bindTransactionInfo(nestedInfo);
            
            return new CustomTransactionStatus(nestedInfo, false);
            
        } catch (SQLException e) {
            log.error("[중첩 트랜잭션 생성 실패] {}", e.getMessage());
            throw new RuntimeException("중첩 트랜잭션 생성 실패", e);
        }
    }
    
    /**
     * 기존 트랜잭션 일시 중단
     * - REQUIRES_NEW에서 사용
     * - 기존 트랜잭션 정보를 백업하고 ThreadLocal에서 제거
     */
    private void suspendCurrentTransaction(CustomTransactionInfo currentTransaction) {
        log.info("[트랜잭션 중단] 기존 트랜잭션 일시 중단");
        CustomTransactionSynchronizationManager.suspendCurrentTransaction();
    }
    
    /**
     * 트랜잭션 커밋
     * - 새 트랜잭션인 경우 실제 Connection.commit() 실행
     * - 참여 트랜잭션인 경우 아무것도 하지 않음 (외부 트랜잭션이 담당)
     */
    public void commit(CustomTransactionStatus status) {
        log.info("[트랜잭션 커밋] 커밋 시작 - 새트랜잭션: {}", status.isNewTransaction());
        
        if (status.isNewTransaction()) {
            try {
                Connection connection = status.getTransactionInfo().getConnection();
                connection.commit();
                log.info("[트랜잭션 커밋] DB 커밋 완료");
                
            } catch (SQLException e) {
                log.error("[트랜잭션 커밋 실패] {}", e.getMessage());
                throw new RuntimeException("커밋 실패", e);
            }
        } else {
            log.info("[트랜잭션 커밋] 참여 트랜잭션 - 실제 커밋은 외부 트랜잭션이 담당");
        }
        
        // 트랜잭션 정리
        cleanupTransaction(status);
    }
    
    /**
     * 트랜잭션 롤백
     * - 새 트랜잭션: 전체 롤백
     * - 중첩 트랜잭션: Savepoint로 롤백
     * - 참여 트랜잭션: rollback-only 마킹
     */
    public void rollback(CustomTransactionStatus status) {
        log.info("[트랜잭션 롤백] 롤백 시작 - 새트랜잭션: {}", status.isNewTransaction());
        
        CustomTransactionInfo transactionInfo = status.getTransactionInfo();
        
        try {
            if (status.isNewTransaction()) {
                // 새 트랜잭션 - 전체 롤백
                Connection connection = transactionInfo.getConnection();
                connection.rollback();
                log.info("[트랜잭션 롤백] DB 전체 롤백 완료");
                
            } else if (transactionInfo.getSavepointName() != null) {
                // 중첩 트랜잭션 - Savepoint로 롤백
                Connection connection = transactionInfo.getConnection();
                connection.rollback(connection.setSavepoint(transactionInfo.getSavepointName()));
                log.info("[트랜잭션 롤백] Savepoint 롤백 완료: {}", transactionInfo.getSavepointName());
                
            } else {
                // 참여 트랜잭션 - rollback-only 마킹
                transactionInfo.setRollbackOnly(true);
                log.info("[트랜잭션 롤백] 참여 트랜잭션 rollback-only 마킹");
            }
            
        } catch (SQLException e) {
            log.error("[트랜잭션 롤백 실패] {}", e.getMessage());
            throw new RuntimeException("롤백 실패", e);
        }
        
        // 트랜잭션 정리
        cleanupTransaction(status);
    }
    
    /**
     * 트랜잭션 정리
     * - Connection 설정 복원 및 반환
     * - ThreadLocal 정보 정리
     */
    private void cleanupTransaction(CustomTransactionStatus status) {
        CustomTransactionInfo transactionInfo = status.getTransactionInfo();
        
        if (status.isNewTransaction()) {
            try {
                Connection connection = transactionInfo.getConnection();
                
                // Connection 설정 복원
                connection.setAutoCommit(true);
                if (transactionInfo.getDefinition().isReadOnly()) {
                    connection.setReadOnly(false);
                }
                
                // Connection 반환
                connection.close();
                log.info("[트랜잭션 정리] Connection 반환 완료");
                
            } catch (SQLException e) {
                log.error("[트랜잭션 정리 실패] {}", e.getMessage());
            }
        }
        
        // ThreadLocal 정리 및 이전 트랜잭션 복원
        CustomTransactionSynchronizationManager.cleanupTransactionInfo();
        log.info("[트랜잭션 정리] ThreadLocal 정리 완료");
    }
    
    /**
     * 격리 수준 변환 유틸리티
     * - 커스텀 Isolation enum을 JDBC 상수로 변환
     */
    private int convertIsolationLevel(Isolation isolation) {
        switch (isolation) {
            case READ_UNCOMMITTED:
                return Connection.TRANSACTION_READ_UNCOMMITTED;
            case READ_COMMITTED:
                return Connection.TRANSACTION_READ_COMMITTED;
            case REPEATABLE_READ:
                return Connection.TRANSACTION_REPEATABLE_READ;
            case SERIALIZABLE:
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return Connection.TRANSACTION_READ_COMMITTED;
        }
    }
}
