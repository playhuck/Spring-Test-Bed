package com.side.springtestbed.transactional.propagation;

import com.side.springtestbed.transactional.customtransactional.core.CustomTransactionInfo;
import com.side.springtestbed.transactional.customtransactional.core.CustomTransactionSynchronizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 트랜잭션 전파 분석기
 * - 트랜잭션 상태 변화를 실시간으로 추적하고 분석
 * - 스레드와 트랜잭션의 관계를 시각화
 */
@Slf4j
@Component
public class PropagationAnalyzer {
    
    /**
     * 트랜잭션 상태 스냅샷
     */
    public static class TransactionSnapshot {
        public final long timestamp;
        public final String threadName;
        public final long threadId;
        public final int stackDepth;
        public final boolean hasActiveTransaction;
        public final String transactionDetails;
        public final String location;
        
        public TransactionSnapshot(String location) {
            this.timestamp = System.currentTimeMillis();
            Thread currentThread = Thread.currentThread();
            this.threadName = currentThread.getName();
            this.threadId = currentThread.getId();
            this.stackDepth = CustomTransactionSynchronizationManager.getTransactionStackDepth();
            this.hasActiveTransaction = CustomTransactionSynchronizationManager.hasCurrentTransaction();
            this.transactionDetails = CustomTransactionSynchronizationManager.getDebugInfo();
            this.location = location;
        }
        
        @Override
        public String toString() {
            return String.format(
                "[%s] Thread[%d:%s] Stack[%d] Active[%s]",
                location, threadId, threadName, stackDepth, hasActiveTransaction
            );
        }
    }
    
    private final List<TransactionSnapshot> snapshots = new ArrayList<>();
    
    /**
     * 현재 트랜잭션 상태 캡처
     * - 특정 시점의 트랜잭션 상태를 스냅샷으로 저장
     */
    public TransactionSnapshot captureSnapshot(String location) {
        TransactionSnapshot snapshot = new TransactionSnapshot(location);
        snapshots.add(snapshot);
        
        log.info("[스냅샷 캡처] {}", snapshot);
        return snapshot;
    }
    
    /**
     * 트랜잭션 상태 변화 분석 리포트 생성
     * - 캡처된 스냅샷들을 분석하여 리포트 생성
     */
    public void generateAnalysisReport() {
        log.info("\n" + "=".repeat(100));
        log.info("트랜잭션 전파 분석 리포트");
        log.info("=".repeat(100));
        
        if (snapshots.isEmpty()) {
            log.info("분석할 스냅샷이 없습니다.");
            return;
        }
        
        // 1. 기본 통계
        generateBasicStatistics();
        
        // 2. 스레드 분석
        analyzeThreadUsage();
        
        // 3. 트랜잭션 스택 변화 분석
        analyzeStackDepthChanges();
        
        // 4. 시간순 상세 분석
        generateTimelineAnalysis();
        
        log.info("=".repeat(100));
    }
    
    /**
     * 기본 통계 생성
     */
    private void generateBasicStatistics() {
        log.info("\n[1. 기본 통계]");
        log.info("총 스냅샷 수: {}", snapshots.size());
        
        long totalExecutionTime = snapshots.isEmpty() ? 0 : 
            snapshots.get(snapshots.size() - 1).timestamp - snapshots.get(0).timestamp;
        log.info("총 실행 시간: {}ms", totalExecutionTime);
        
        int maxStackDepth = snapshots.stream()
            .mapToInt(s -> s.stackDepth)
            .max()
            .orElse(0);
        log.info("최대 트랜잭션 스택 깊이: {}", maxStackDepth);
        
        long activeTransactionCount = snapshots.stream()
            .mapToLong(s -> s.hasActiveTransaction ? 1 : 0)
            .sum();
        log.info("활성 트랜잭션이 있던 스냅샷: {}/{}", activeTransactionCount, snapshots.size());
    }
    
    /**
     * 스레드 사용 분석
     */
    private void analyzeThreadUsage() {
        log.info("\n[2. 스레드 분석]");
        
        // 사용된 스레드 목록
        snapshots.stream()
            .map(s -> String.format("Thread[%d:%s]", s.threadId, s.threadName))
            .distinct()
            .forEach(threadInfo -> log.info("사용된 스레드: {}", threadInfo));
        
        // 스레드 일관성 검증
        boolean singleThread = snapshots.stream()
            .mapToLong(s -> s.threadId)
            .distinct()
            .count() == 1;
        
        if (singleThread) {
            log.info("✅ 모든 트랜잭션이 단일 스레드에서 실행됨");
        } else {
            log.warn("⚠️ 여러 스레드에서 트랜잭션이 실행됨 (예상되지 않음)");
        }
    }
    
    /**
     * 트랜잭션 스택 깊이 변화 분석
     */
    private void analyzeStackDepthChanges() {
        log.info("\n[3. 트랜잭션 스택 깊이 변화]");
        
        for (int i = 0; i < snapshots.size(); i++) {
            TransactionSnapshot snapshot = snapshots.get(i);
            String changeIndicator = "";
            
            if (i > 0) {
                int prevDepth = snapshots.get(i - 1).stackDepth;
                int currentDepth = snapshot.stackDepth;
                
                if (currentDepth > prevDepth) {
                    changeIndicator = " ↗️ 새 트랜잭션 시작";
                } else if (currentDepth < prevDepth) {
                    changeIndicator = " ↘️ 트랜잭션 종료";
                } else {
                    changeIndicator = " ➡️ 동일 레벨";
                }
            }
            
            log.info("{}. [{}] 스택 깊이: {}{}", 
                i + 1, snapshot.location, snapshot.stackDepth, changeIndicator);
        }
    }
    
    /**
     * 시간순 상세 분석
     */
    private void generateTimelineAnalysis() {
        log.info("\n[4. 시간순 상세 분석]");
        
        long baseTime = snapshots.isEmpty() ? 0 : snapshots.get(0).timestamp;
        
        for (int i = 0; i < snapshots.size(); i++) {
            TransactionSnapshot snapshot = snapshots.get(i);
            long relativeTime = snapshot.timestamp - baseTime;
            
            log.info("\n시점 {}: +{}ms", i + 1, relativeTime);
            log.info("  위치: {}", snapshot.location);
            log.info("  스레드: {}[{}]", snapshot.threadName, snapshot.threadId);
            log.info("  트랜잭션 스택: {}", snapshot.stackDepth);
            log.info("  활성 상태: {}", snapshot.hasActiveTransaction);
            
            if (snapshot.hasActiveTransaction && !snapshot.transactionDetails.equals("No active transaction")) {
                // 트랜잭션 상세 정보 출력 (들여쓰기 적용)
                String[] lines = snapshot.transactionDetails.split("\n");
                for (String line : lines) {
                    log.info("    {}", line);
                }
            }
        }
    }
    
    /**
     * 특정 전파 정책의 패턴 검증
     */
    public void validatePropagationPattern(String propagationType) {
        log.info("\n[전파 정책 패턴 검증: {}]", propagationType);
        
        switch (propagationType.toUpperCase()) {
            case "REQUIRED":
                validateRequiredPattern();
                break;
            case "REQUIRES_NEW":
                validateRequiresNewPattern();
                break;
            case "NESTED":
                validateNestedPattern();
                break;
            default:
                log.warn("알 수 없는 전파 정책: {}", propagationType);
        }
    }
    
    private void validateRequiredPattern() {
        // REQUIRED는 스택 깊이가 1을 유지해야 함
        boolean validRequired = snapshots.stream()
            .filter(s -> s.hasActiveTransaction)
            .allMatch(s -> s.stackDepth <= 1);
        
        if (validRequired) {
            log.info("✅ REQUIRED 패턴 검증 성공: 모든 트랜잭션이 같은 레벨에서 실행됨");
        } else {
            log.error("❌ REQUIRED 패턴 검증 실패: 예상과 다른 스택 깊이 발견");
        }
    }
    
    private void validateRequiresNewPattern() {
        // REQUIRES_NEW는 스택 깊이 증가와 감소가 있어야 함
        int maxDepth = snapshots.stream()
            .mapToInt(s -> s.stackDepth)
            .max()
            .orElse(0);
        
        if (maxDepth > 1) {
            log.info("✅ REQUIRES_NEW 패턴 검증 성공: 새로운 트랜잭션 레벨이 생성됨 (최대 깊이: {})", maxDepth);
        } else {
            log.error("❌ REQUIRES_NEW 패턴 검증 실패: 새로운 트랜잭션 레벨이 생성되지 않음");
        }
    }
    
    private void validateNestedPattern() {
        // NESTED는 같은 Connection을 사용하므로 특별한 패턴은 없지만
        // 스택 깊이 변화는 있어야 함
        validateRequiresNewPattern(); // 유사한 검증 로직 재사용
    }
    
    /**
     * 스냅샷 초기화
     */
    public void clearSnapshots() {
        snapshots.clear();
        log.info("[스냅샷 초기화] 모든 스냅샷이 삭제되었습니다.");
    }
    
    /**
     * 현재 저장된 스냅샷 개수 반환
     */
    public int getSnapshotCount() {
        return snapshots.size();
    }
}
