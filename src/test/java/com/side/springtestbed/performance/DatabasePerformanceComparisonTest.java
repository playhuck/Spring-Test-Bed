package com.side.springtestbed.performance;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("데이터베이스 성능 비교 테스트 (tmpfs PostgreSQL vs H2)")
class DatabasePerformanceComparisonTest {

    private static final Logger log = LoggerFactory.getLogger(DatabasePerformanceComparisonTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("performancetest")
            .withUsername("test")
            .withPassword("test")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw,noexec,nosuid,size=2g"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Test
    @Transactional
    @DisplayName("tmpfs PostgreSQL 대량 처리 성능 테스트")
    void testTmpfsPostgreSQLPerformance() {
        log.info("=== TMPFS POSTGRESQL 성능 테스트 시작 ===");

        long overallStartTime = System.currentTimeMillis();

        // 1. 단일 INSERT 성능 테스트
        long singleInsertStart = System.currentTimeMillis();
        for (int i = 1; i <= 100; i++) {
            Post post = Post.builder()
                    .title("PostgreSQL 단일 INSERT 테스트 " + i)
                    .content("PostgreSQL tmpfs 단일 INSERT 성능 테스트 내용 " + i)
                    .createdDate(LocalDateTime.now())
                    .build();
            postRepository.save(post);
        }
        long singleInsertEnd = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] 단일 INSERT 100개: {}ms", singleInsertEnd - singleInsertStart);

        // 2. 관계형 데이터 INSERT 성능 테스트
        long relationInsertStart = System.currentTimeMillis();
        for (int i = 1; i <= 100; i++) {
            Post post = Post.builder()
                    .title("PostgreSQL 관계형 INSERT 테스트 " + i)
                    .content("PostgreSQL tmpfs 관계형 INSERT 성능 테스트 내용 " + i)
                    .createdDate(LocalDateTime.now())
                    .build();

            // 각 포스트에 5개의 댓글 추가
            for (int j = 1; j <= 5; j++) {
                PostComment comment = PostComment.builder()
                        .review("PostgreSQL 포스트 " + i + "의 댓글 " + j)
                        .build();
                post.addComment(comment);
            }
            postRepository.save(post);
        }
        long relationInsertEnd = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] 관계형 INSERT 100개(댓글 500개): {}ms", relationInsertEnd - relationInsertStart);

        // 3. 대량 SELECT 성능 테스트
        long selectStart = System.currentTimeMillis();
        long totalPosts = postRepository.count();
        long selectEnd = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] COUNT 쿼리: {}ms (총 {}개)", selectEnd - selectStart, totalPosts);

        // 4. 검색 성능 테스트
        long searchStart = System.currentTimeMillis();
        var searchResults = postRepository.findByTitleContaining("INSERT 테스트");
        long searchEnd = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] LIKE 검색: {}ms ({}개 결과)", searchEnd - searchStart, searchResults.size());

        long overallEndTime = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] 전체 테스트 시간: {}ms", overallEndTime - overallStartTime);

        assertThat(totalPosts).isEqualTo(200);
        assertThat(commentRepository.count()).isEqualTo(500);

        log.info("=== TMPFS POSTGRESQL 성능 테스트 완료 ===");
    }

    @Test
    @DisplayName("성능 비교 결과 요약")
    void performanceComparisonSummary() {
        log.info("========================================");
        log.info("        성능 비교 테스트 요약           ");
        log.info("========================================");
        log.info("");
        log.info("테스트 환경:");
        log.info("  - tmpfs PostgreSQL: RAM 기반 스토리지 (2GB 할당)");
        log.info("  - H2 인메모리: 완전히 메모리 기반");
        log.info("");
        log.info("테스트 항목:");
        log.info("  1. 단일 INSERT 성능 (100건)");
        log.info("  2. 관계형 INSERT 성능 (100건 + 댓글 500건)");
        log.info("  3. COUNT 쿼리 성능");
        log.info("  4. LIKE 검색 성능");
        log.info("");
        log.info("예상 결과:");
        log.info("  - H2 인메모리: 가장 빠른 성능 (메모리 전용)");
        log.info("  - tmpfs PostgreSQL: 중간 성능 (RAM 스토리지 + PostgreSQL 기능)");
        log.info("  - 일반 PostgreSQL: 가장 느린 성능 (디스크 기반)");
        log.info("");
        log.info("실제 성능 비교는 각 테스트 클래스의 로그를 확인하세요:");
        log.info("  - H2InMemoryIntegrationTest");
        log.info("  - TmpfsPostgreSQLIntegrationTest");
        log.info("========================================");
    }
}
