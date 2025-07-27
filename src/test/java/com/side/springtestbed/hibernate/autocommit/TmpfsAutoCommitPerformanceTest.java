package com.side.springtestbed.hibernate.autocommit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@DisplayName("tmpfs를 활용한 AutoCommit 성능 비교 테스트")
public class TmpfsAutoCommitPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(TmpfsAutoCommitPerformanceTest.class);

    private static final int INSERT_COUNT = 1000;
    private static final int TEST_ITERATIONS = 5;
    private static final int WARMUP_ITERATIONS = 2;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw,noexec,nosuid,size=1g")); // tmpfs 설정

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:10.11")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withTmpFs(Map.of("/var/lib/mysql", "rw,noexec,nosuid,size=1g")); // tmpfs 설정

    @Test
    @DisplayName("PostgreSQL tmpfs AutoCommit 성능 테스트")
    void testPostgreSQLAutoCommitPerformance() throws SQLException {
        log.info("=== PostgreSQL tmpfs AutoCommit 성능 테스트 시작 ===");

        DatabaseConfig config = new DatabaseConfig(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            postgres.getPassword(),
            "PostgreSQL"
        );

        PerformanceResult result = performAutoCommitTest(config);
        printDetailedResults("PostgreSQL (tmpfs)", result);
    }

    @Test
    @DisplayName("MariaDB tmpfs AutoCommit 성능 테스트")
    void testMariaDBAutoCommitPerformance() throws SQLException {
        log.info("=== MariaDB tmpfs AutoCommit 성능 테스트 시작 ===");

        DatabaseConfig config = new DatabaseConfig(
            mariadb.getJdbcUrl(),
            mariadb.getUsername(),
            mariadb.getPassword(),
            "MariaDB"
        );

        PerformanceResult result = performAutoCommitTest(config);
        printDetailedResults("MariaDB (tmpfs)", result);
    }

    @Test
    @DisplayName("연결 리스 시간 측정 테스트 (tmpfs)")
    void testConnectionLeaseTime() throws SQLException {
        log.info("=== 연결 리스 시간 측정 테스트 시작 ===");
        
        long warmUpDuration = TimeUnit.SECONDS.toNanos(3);
        long measurementsDuration = TimeUnit.SECONDS.toNanos(10);
        
        DatabaseConfig postgresConfig = new DatabaseConfig(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "PostgreSQL");
        DatabaseConfig mariaConfig = new DatabaseConfig(
            mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword(), "MariaDB");
        
        // PostgreSQL 연결 리스 시간 측정
        log.info("🐘 PostgreSQL 연결 리스 시간 측정");
        measureConnectionLeaseTime(postgresConfig, warmUpDuration, measurementsDuration);
        
        // MariaDB 연결 리스 시간 측정
        log.info("🐬 MariaDB 연결 리스 시간 측정");
        measureConnectionLeaseTime(mariaConfig, warmUpDuration, measurementsDuration);
    }

    @Test
    @DisplayName("Batch Insert vs Single Insert 성능 비교 (tmpfs)")
    void compareBatchVsSingleInsert() throws SQLException {
        log.info("=== Batch vs Single Insert 성능 비교 (tmpfs) ===");

        // PostgreSQL Batch vs Single 비교
        log.info("🔹 PostgreSQL Batch vs Single Insert");
        DatabaseConfig postgresConfig = new DatabaseConfig(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), "PostgreSQL");
        
        long postgresSingle = performSingleInserts(postgresConfig);
        long postgresBatch = performBatchInserts(postgresConfig);
        
        log.info("PostgreSQL Single: {}ms, Batch: {}ms", postgresSingle, postgresBatch);
        if (postgresBatch < postgresSingle) {
            log.info("PostgreSQL Batch는 Single보다 {:.1f}x 빠름", (double)postgresSingle / postgresBatch);
        }

        // MariaDB Batch vs Single 비교  
        log.info("🔹 MariaDB Batch vs Single Insert");
        DatabaseConfig mariaConfig = new DatabaseConfig(
            mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword(), "MariaDB");
            
        long mariaSingle = performSingleInserts(mariaConfig);
        long mariaBatch = performBatchInserts(mariaConfig);
        
        log.info("MariaDB Single: {}ms, Batch: {}ms", mariaSingle, mariaBatch);
        if (mariaBatch < mariaSingle) {
            log.info("MariaDB Batch는 Single보다 {:.1f}x 빠름", (double)mariaSingle / mariaBatch);
        }
    }

    private void measureConnectionLeaseTime(DatabaseConfig config, long warmUpDuration, long measurementsDuration) throws SQLException {
        // Warmup 단계
        log.info("  Warming up for {}s...", TimeUnit.NANOSECONDS.toSeconds(warmUpDuration));
        long warmUpThreshold = System.nanoTime() + warmUpDuration;
        
        while (System.nanoTime() < warmUpThreshold) {
            performConnectionOperation(config, false); // warmup은 측정하지 않음
        }

        // 실제 측정 단계
        log.info("  Measuring connection lease time for {}s...", TimeUnit.NANOSECONDS.toSeconds(measurementsDuration));
        long measurementsThreshold = System.nanoTime() + measurementsDuration;
        
        List<Long> connectionTimes = new ArrayList<>();
        int operationCount = 0;
        
        while (System.nanoTime() < measurementsThreshold) {
            long leaseTime = performConnectionOperation(config, true);
            if (leaseTime > 0) {
                connectionTimes.add(leaseTime);
            }
            operationCount++;
        }
        
        // 결과 출력
        if (!connectionTimes.isEmpty()) {
            double avgLeaseTime = connectionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long minLeaseTime = connectionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxLeaseTime = connectionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            
            log.info("  📊 {} 연결 리스 시간 결과:", config.databaseName);
            log.info("     총 연결 시도: {} 회", operationCount);
            log.info("     측정된 연결: {} 회", connectionTimes.size());
            log.info("     평균 리스 시간: {:.2f}ms", avgLeaseTime / 1_000_000.0);
            log.info("     최소 리스 시간: {:.2f}ms", minLeaseTime / 1_000_000.0);
            log.info("     최대 리스 시간: {:.2f}ms", maxLeaseTime / 1_000_000.0);
            
            // 연결 효율성 평가
            if (avgLeaseTime < 1_000_000) { // 1ms 미만
                log.info("     ✅ 연결 성능: 우수 (< 1ms)");
            } else if (avgLeaseTime < 5_000_000) { // 5ms 미만
                log.info("     ⚠️  연결 성능: 보통 (1-5ms)");
            } else {
                log.info("     ❌ 연결 성능: 개선 필요 (> 5ms)");
            }
        } else {
            log.warn("  ⚠️  측정된 연결 시간이 없습니다.");
        }
    }

    private long performConnectionOperation(DatabaseConfig config, boolean measureTime) throws SQLException {
        long startTime = measureTime ? System.nanoTime() : 0;
        
        try (Connection conn = DriverManager.getConnection(config.url, config.username, config.password)) {
            // 간단한 데이터베이스 작업 수행 (연결 리스 시간 측정 목적)
            conn.setAutoCommit(false);
            
            // 테이블이 존재하는지 확인하고 없으면 생성
            setupTable(conn, config.databaseName);
            
            // 간단한 INSERT/SELECT 작업으로 연결 활용
            String insertSql = "INSERT INTO performance_test (id, name, value, description, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, (int) (System.currentTimeMillis() % 1000000)); // 고유 ID
                pstmt.setString(2, "ConnectionTest");
                pstmt.setInt(3, 1);
                pstmt.setString(4, "Connection lease time test");
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                pstmt.executeUpdate();
            }
            
            // 방금 삽입한 데이터 조회
            String selectSql = "SELECT COUNT(*) FROM performance_test WHERE name = 'ConnectionTest'";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    rs.getInt(1); // 결과 읽기
                }
            }
            
            conn.commit();
            
            if (measureTime) {
                return System.nanoTime() - startTime;
            }
            
        } catch (SQLException e) {
            // 연결 실패시 로그만 남기고 계속 진행
            if (measureTime) {
                log.debug("Connection operation failed: {}", e.getMessage());
            }
        }
        
        return 0;
    }

    private PerformanceResult performAutoCommitTest(DatabaseConfig config) throws SQLException {
        log.info("데이터베이스 연결 및 테스트 시작: {}", config.databaseName);

        // Warmup
        log.info("Warmup 진행 중...");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performInserts(config, true, false);
            performInserts(config, false, false);
        }

        // AutoCommit ON 테스트
        log.info("AutoCommit ON 테스트 시작");
        List<Long> autoCommitOnTimes = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long time = performInserts(config, true, true);
            autoCommitOnTimes.add(time);
            log.info("  Run {}: {}ms", i + 1, time);
        }

        // AutoCommit OFF 테스트
        log.info("AutoCommit OFF 테스트 시작");
        List<Long> autoCommitOffTimes = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long time = performInserts(config, false, true);
            autoCommitOffTimes.add(time);
            log.info("  Run {}: {}ms", i + 1, time);
        }

        return new PerformanceResult(config.databaseName, autoCommitOnTimes, autoCommitOffTimes);
    }

    private long performInserts(DatabaseConfig config, boolean autoCommit, boolean logResult) throws SQLException {
        try (Connection conn = DriverManager.getConnection(config.url, config.username, config.password)) {
            setupTable(conn, config.databaseName);
            conn.setAutoCommit(autoCommit);

            long startTime = System.nanoTime();

            String sql = "INSERT INTO performance_test (id, name, value, description, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                Timestamp now = new Timestamp(System.currentTimeMillis());
                
                for (int i = 0; i < INSERT_COUNT; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "Test_" + i);
                    pstmt.setInt(3, i * 100);
                    pstmt.setString(4, "Performance test data " + i);
                    pstmt.setTimestamp(5, now);
                    pstmt.executeUpdate();
                }

                if (!autoCommit) {
                    conn.commit();
                }
            }

            long endTime = System.nanoTime();
            return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        }
    }

    private long performSingleInserts(DatabaseConfig config) throws SQLException {
        try (Connection conn = DriverManager.getConnection(config.url, config.username, config.password)) {
            setupTable(conn, config.databaseName);
            conn.setAutoCommit(false);

            long startTime = System.nanoTime();

            String sql = "INSERT INTO performance_test (id, name, value, description, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                
                for (int i = 0; i < INSERT_COUNT; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "Single_" + i);
                    pstmt.setInt(3, i * 200);
                    pstmt.setString(4, "Single insert test " + i);
                    pstmt.setTimestamp(5, now);
                    pstmt.executeUpdate();
                }
                conn.commit();
            }

            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        }
    }

    private long performBatchInserts(DatabaseConfig config) throws SQLException {
        try (Connection conn = DriverManager.getConnection(config.url, config.username, config.password)) {
            setupTable(conn, config.databaseName);
            conn.setAutoCommit(false);

            long startTime = System.nanoTime();

            String sql = "INSERT INTO performance_test (id, name, value, description, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                
                for (int i = 0; i < INSERT_COUNT; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "Batch_" + i);
                    pstmt.setInt(3, i * 200);
                    pstmt.setString(4, "Batch insert test " + i);
                    pstmt.setTimestamp(5, now);
                    pstmt.addBatch();

                    // 100개씩 배치 실행
                    if (i % 100 == 0 && i > 0) {
                        pstmt.executeBatch();
                    }
                }
                pstmt.executeBatch(); // 나머지 실행
                conn.commit();
            }

            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        }
    }

    private void setupTable(Connection conn, String databaseName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS performance_test");
            
            String createTableSql;
            if (databaseName.equals("PostgreSQL")) {
                createTableSql = """
                    CREATE TABLE performance_test (
                        id INTEGER PRIMARY KEY,
                        name VARCHAR(100),
                        value INTEGER,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
            } else { // MariaDB
                createTableSql = """
                    CREATE TABLE performance_test (
                        id INTEGER PRIMARY KEY,
                        name VARCHAR(100),
                        value INTEGER,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;
            }
            
            stmt.execute(createTableSql);
        }
    }

    private void printDetailedResults(String databaseName, PerformanceResult result) {
        log.info("📊 {} 상세 결과:", databaseName);
        log.info("   AutoCommit ON:  평균 {:.1f}ms (최소: {}ms, 최대: {}ms)", 
            result.getAvgAutoCommitOn(), result.getMinAutoCommitOn(), result.getMaxAutoCommitOn());
        log.info("   AutoCommit OFF: 평균 {:.1f}ms (최소: {}ms, 최대: {}ms)", 
            result.getAvgAutoCommitOff(), result.getMinAutoCommitOff(), result.getMaxAutoCommitOff());
        
        if (result.getAvgAutoCommitOff() < result.getAvgAutoCommitOn()) {
            double speedup = result.getAvgAutoCommitOn() / result.getAvgAutoCommitOff();
            double improvement = ((result.getAvgAutoCommitOn() - result.getAvgAutoCommitOff()) / result.getAvgAutoCommitOn()) * 100;
            log.info("   ✅ AutoCommit OFF가 {:.1f}x 빠름 ({:.1f}% 성능 향상)", speedup, improvement);
        } else {
            double slowdown = result.getAvgAutoCommitOff() / result.getAvgAutoCommitOn();
            log.info("   ⚠️  AutoCommit ON이 {:.1f}x 빠름", slowdown);
        }
    }

    private void printComparisonResults(PerformanceResult postgresResult, PerformanceResult mariaResult) {
        log.info("📈 PostgreSQL vs MariaDB 성능 비교 결과");
        log.info("═".repeat(70));
        
        log.info("🐘 PostgreSQL (tmpfs):");
        log.info("   AutoCommit ON:  {:.1f}ms", postgresResult.getAvgAutoCommitOn());
        log.info("   AutoCommit OFF: {:.1f}ms", postgresResult.getAvgAutoCommitOff());
        
        log.info("🐬 MariaDB (tmpfs):");
        log.info("   AutoCommit ON:  {:.1f}ms", mariaResult.getAvgAutoCommitOn());
        log.info("   AutoCommit OFF: {:.1f}ms", mariaResult.getAvgAutoCommitOff());
        
        log.info("🏆 AutoCommit ON 비교:");
        if (postgresResult.getAvgAutoCommitOn() < mariaResult.getAvgAutoCommitOn()) {
            double ratio = mariaResult.getAvgAutoCommitOn() / postgresResult.getAvgAutoCommitOn();
            log.info("   PostgreSQL이 {:.1f}x 빠름", ratio);
        } else {
            double ratio = postgresResult.getAvgAutoCommitOn() / mariaResult.getAvgAutoCommitOn();
            log.info("   MariaDB가 {:.1f}x 빠름", ratio);
        }
        
        log.info("🏆 AutoCommit OFF 비교:");
        if (postgresResult.getAvgAutoCommitOff() < mariaResult.getAvgAutoCommitOff()) {
            double ratio = mariaResult.getAvgAutoCommitOff() / postgresResult.getAvgAutoCommitOff();
            log.info("   PostgreSQL이 {:.1f}x 빠름", ratio);
        } else {
            double ratio = postgresResult.getAvgAutoCommitOff() / mariaResult.getAvgAutoCommitOff();
            log.info("   MariaDB가 {:.1f}x 빠름", ratio);
        }
        
        // 전체적으로 더 나은 데이터베이스 추천
        double postgresAvg = (postgresResult.getAvgAutoCommitOn() + postgresResult.getAvgAutoCommitOff()) / 2;
        double mariaAvg = (mariaResult.getAvgAutoCommitOn() + mariaResult.getAvgAutoCommitOff()) / 2;
        
        if (postgresAvg < mariaAvg) {
            log.info("🎯 종합 추천: PostgreSQL (전체 평균 {:.1f}ms vs {:.1f}ms)", postgresAvg, mariaAvg);
        } else {
            log.info("🎯 종합 추천: MariaDB (전체 평균 {:.1f}ms vs {:.1f}ms)", mariaAvg, postgresAvg);
        }
    }

    private double calculateAverage(List<Long> times) {
        return times.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    static class DatabaseConfig {
        final String url;
        final String username;
        final String password;
        final String databaseName;

        DatabaseConfig(String url, String username, String password, String databaseName) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.databaseName = databaseName;
        }
    }

    static class PerformanceResult {
        private final String databaseName;
        private final List<Long> autoCommitOnTimes;
        private final List<Long> autoCommitOffTimes;

        public PerformanceResult(String databaseName, List<Long> autoCommitOnTimes, List<Long> autoCommitOffTimes) {
            this.databaseName = databaseName;
            this.autoCommitOnTimes = new ArrayList<>(autoCommitOnTimes);
            this.autoCommitOffTimes = new ArrayList<>(autoCommitOffTimes);
        }

        public String getDatabaseName() { return databaseName; }
        
        public double getAvgAutoCommitOn() {
            return autoCommitOnTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
        
        public double getAvgAutoCommitOff() {
            return autoCommitOffTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
        
        public long getMinAutoCommitOn() {
            return autoCommitOnTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        }
        
        public long getMaxAutoCommitOn() {
            return autoCommitOnTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        }
        
        public long getMinAutoCommitOff() {
            return autoCommitOffTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        }
        
        public long getMaxAutoCommitOff() {
            return autoCommitOffTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        }
    }
}
