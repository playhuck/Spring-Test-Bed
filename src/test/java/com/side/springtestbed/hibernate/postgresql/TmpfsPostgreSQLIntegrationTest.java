package com.side.springtestbed.hibernate.postgresql;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import com.side.springtestbed.hibernate.postgresql.entity.PostDetails;
import com.side.springtestbed.hibernate.postgresql.entity.Tag;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("tmpfs를 활용한 PostgreSQL 통합테스트")
class TmpfsPostgreSQLIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TmpfsPostgreSQLIntegrationTest.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw,noexec,nosuid,size=1g")); // tmpfs 설정

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> 500);
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> true);
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> true);
        registry.add("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", () -> true);
//        registry.add("spring.jpa.properties.hibernate.connection.provider_disables_autocommit", () -> true);
        // Hibernate가 JDBC 드라이버의 자동 커밋 기능을 비활성화하고, 트랜잭션 관리자가 명시적으로 커밋/롤백을 제어
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("tmpfs PostgreSQL 컨테이너 연결 테스트")
    void testTmpfsPostgreSQLConnection() {
        assertThat(postgres.isRunning()).isTrue();
        log.info("[TMPFS-POSTGRESQL] 컨테이너가 성공적으로 실행 중입니다");
        log.info("[TMPFS-POSTGRESQL] JDBC URL: {}", postgres.getJdbcUrl());
        log.info("[TMPFS-POSTGRESQL] tmpfs 설정: /var/lib/postgresql/data는 RAM에서 실행됩니다");
    }

//    @Test
//    @Transactional
//    @DisplayName("Post CRUD TEST")
//    void testPostEntityCRUD() {
//        log.info("[TMPFS-POSTGRESQL] Post 엔티티 CRUD 테스트 시작");
//
//        // Create
//        Post post = Post.builder()
//                .title("tmpfs 테스트 포스트")
//                .content("tmpfs PostgreSQL 성능 테스트용 내용")
//                .createdDate(LocalDateTime.now())
//                .build();
//
//        Post savedPost = postRepository.save(post);
//        assertThat(savedPost.getId()).isNotNull();
//        log.info("[TMPFS-POSTGRESQL] Create: Post 생성 완료 (ID: {})", savedPost.getId());
//
//        // Read
//        Optional<Post> foundPost = postRepository.findById(savedPost.getId());
//        assertThat(foundPost).isPresent();
//        assertThat(foundPost.get().getTitle()).isEqualTo("tmpfs 테스트 포스트");
//        log.info("[TMPFS-POSTGRESQL] Read: Post 조회 완료");
//
//        // Update
//        foundPost.get().setTitle("수정된 제목");
//        Post updatedPost = postRepository.save(foundPost.get());
//        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
//        log.info("[TMPFS-POSTGRESQL] Update: Post 수정 완료");
//
//        // Delete
//        postRepository.delete(updatedPost);
//        Optional<Post> deletedPost = postRepository.findById(updatedPost.getId());
//        assertThat(deletedPost).isEmpty();
//        log.info("[TMPFS-POSTGRESQL] Delete: Post 삭제 완료");
//
//        log.info("[TMPFS-POSTGRESQL] Post CRUD 테스트 완료");
//    }

    @Test
    @Transactional
    @DisplayName("tmpts 성능 테스트 - 대량 데이터 처리")
    void testTmpfsPerformanceWithBulkData() {
        log.info("tmpts Postgresql Bulk 처리 시작");

        long startTime = System.currentTimeMillis();

        List<Post> postList = new ArrayList<>(50000);

        // 대량 데이터 생성 (500개 포스트, 각각 3개 댓글)
        for (int i = 1; i <= 50000; i++) {
            Post post = Post.builder()
                    .title("H2 성능 테스트 포스트 " + i)
                    .content("H2 인메모리 데이터베이스 성능 테스트용 내용 " + i)
                    .createdDate(LocalDateTime.now())
                    .build();

            PostComment comment = PostComment.builder()
                    .review("포스트 " + i + "의 댓글 " + i)
                    .build();
            post.addComment(comment);

            postList.add(post);
        }

        postRepository.saveAll(postList);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 결과 검증
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        assertThat(totalPosts).isEqualTo(50000);
        assertThat(totalComments).isEqualTo(50000);

        log.info("tmpts Postgresql 포스트 수: {}", totalPosts);
        log.info("tmpts Postgresql 댓글 수: {}", totalComments);
        log.info("tmpts Postgresql 총 처리 시간: {}ms", duration);

        long searchStartTime = System.currentTimeMillis();
        List<Post> searchResults = postRepository.findByTitleContaining("성능 테스트");
        long searchEndTime = System.currentTimeMillis();
        long searchDuration = searchEndTime - searchStartTime;

        assertThat(searchResults).hasSize(50000);
        log.info("tmpts Postgresql 대량 검색 테스트 완료: {} 건 조회 (검색 시간: {}ms)", searchResults.size(), searchDuration);

        log.info("tmpts Postgresql 성능 테스트 완료");
    }

}
