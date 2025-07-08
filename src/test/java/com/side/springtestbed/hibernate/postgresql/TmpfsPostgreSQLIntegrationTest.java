package com.side.springtestbed.hibernate.postgresql;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import com.side.springtestbed.hibernate.postgresql.entity.PostDetails;
import com.side.springtestbed.hibernate.postgresql.entity.Tag;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("tmpfs를 활용한 PostgreSQL 통합테스트")
class TmpfsPostgreSQLIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TmpfsPostgreSQLIntegrationTest.class);

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

    @Test
    @Transactional
    @DisplayName("Post 엔티티 기본 CRUD 테스트")
    void testPostEntityCRUD() {
        log.info("[TMPFS-POSTGRESQL] Post 엔티티 CRUD 테스트 시작");

        // Create
        Post post = Post.builder()
                .title("tmpfs 테스트 포스트")
                .content("tmpfs PostgreSQL 성능 테스트용 내용")
                .createdDate(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        assertThat(savedPost.getId()).isNotNull();
        log.info("[TMPFS-POSTGRESQL] Create: Post 생성 완료 (ID: {})", savedPost.getId());

        // Read
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("tmpfs 테스트 포스트");
        log.info("[TMPFS-POSTGRESQL] Read: Post 조회 완료");

        // Update
        foundPost.get().setTitle("수정된 제목");
        Post updatedPost = postRepository.save(foundPost.get());
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        log.info("[TMPFS-POSTGRESQL] Update: Post 수정 완료");

        // Delete
        postRepository.delete(updatedPost);
        Optional<Post> deletedPost = postRepository.findById(updatedPost.getId());
        assertThat(deletedPost).isEmpty();
        log.info("[TMPFS-POSTGRESQL] Delete: Post 삭제 완료");

        log.info("[TMPFS-POSTGRESQL] Post CRUD 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("tmpfs 성능 테스트 - 대량 데이터 처리")
    void testTmpfsPerformanceWithBulkData() {
        log.info("[TMPFS-POSTGRESQL] 성능 테스트 시작 - 대량 데이터 처리");

        long startTime = System.currentTimeMillis();

        // 대량 데이터 생성 (500개 포스트, 각각 3개 댓글)
        for (int i = 1; i <= 500; i++) {
            Post post = Post.builder()
                    .title("tmpfs 성능 테스트 포스트 " + i)
                    .content("tmpfs PostgreSQL 성능 테스트용 내용 " + i)
                    .createdDate(LocalDateTime.now())
                    .build();

            // 각 포스트에 3개의 댓글 추가
            for (int j = 1; j <= 3; j++) {
                PostComment comment = PostComment.builder()
                        .review("포스트 " + i + "의 댓글 " + j)
                        .build();
                post.addComment(comment);
            }

            postRepository.save(post);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 결과 검증
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        assertThat(totalPosts).isEqualTo(500);
        assertThat(totalComments).isEqualTo(1500);

        log.info("[TMPFS-POSTGRESQL] 대량 데이터 처리 완료:");
        log.info("[TMPFS-POSTGRESQL] 포스트 수: {}", totalPosts);
        log.info("[TMPFS-POSTGRESQL] 댓글 수: {}", totalComments);
        log.info("[TMPFS-POSTGRESQL] 총 처리 시간: {}ms", duration);
        log.info("[TMPFS-POSTGRESQL] 평균 처리 시간: {}ms/post", duration / totalPosts);

        // 대량 검색 테스트
        long searchStartTime = System.currentTimeMillis();
        List<Post> searchResults = postRepository.findByTitleContaining("성능 테스트");
        long searchEndTime = System.currentTimeMillis();
        long searchDuration = searchEndTime - searchStartTime;

        assertThat(searchResults).hasSize(500);
        log.info("[TMPFS-POSTGRESQL] 대량 검색 테스트 완료: {} 건 조회 (검색 시간: {}ms)", searchResults.size(), searchDuration);

        log.info("[TMPFS-POSTGRESQL] 성능 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("PostgreSQL 특화 기능 테스트")
    void testPostgreSQLSpecificFeatures() {
        log.info("[TMPFS-POSTGRESQL] PostgreSQL 특화 기능 테스트 시작");

        // 테스트 데이터 생성
        Post post1 = postRepository.save(Post.builder()
                .title("PostgreSQL Tutorial")
                .content("PostgreSQL 튜토리얼 내용")
                .createdDate(LocalDateTime.now())
                .build());
        Post post2 = postRepository.save(Post.builder()
                .title("Advanced PostgreSQL")
                .content("고급 PostgreSQL 내용")
                .createdDate(LocalDateTime.now())
                .build());
        Post post3 = postRepository.save(Post.builder()
                .title("MySQL vs PostgreSQL")
                .content("MySQL과 PostgreSQL 비교")
                .createdDate(LocalDateTime.now())
                .build());

        // 정규식 검색 (PostgreSQL 특화)
        long regexStartTime = System.currentTimeMillis();
        List<Post> regexResults = postRepository.findByTitleRegex(".*PostgreSQL.*");
        long regexEndTime = System.currentTimeMillis();
        
        assertThat(regexResults).hasSize(3);
        log.info("[TMPFS-POSTGRESQL] 정규식 검색 테스트 완료: {} 건 (검색 시간: {}ms)", 
                regexResults.size(), regexEndTime - regexStartTime);

        // Full Text Search 테스트
        long fullTextStartTime = System.currentTimeMillis();
        List<Post> fullTextResults = postRepository.fullTextSearch("PostgreSQL tutorial");
        long fullTextEndTime = System.currentTimeMillis();
        
        log.info("[TMPFS-POSTGRESQL] Full Text Search 테스트 완료: {} 건 (검색 시간: {}ms)", 
                fullTextResults.size(), fullTextEndTime - fullTextStartTime);

        log.info("[TMPFS-POSTGRESQL] PostgreSQL 특화 기능 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("복합 관계 및 N+1 문제 해결 테스트")
    void testComplexRelationshipsAndN1Prevention() {
        log.info("[TMPFS-POSTGRESQL] 복합 관계 및 N+1 문제 해결 테스트 시작");

        long startTime = System.currentTimeMillis();

        // 완전한 Post 엔티티 생성 (모든 관계 포함)
        Post post = Post.builder()
                .title("모든 관계를 가진 포스트")
                .content("복합 관계 테스트용 내용")
                .createdDate(LocalDateTime.now())
                .build();

        // PostDetails 추가
        PostDetails details = PostDetails.builder()
                .createdBy("admin")
                .post(post)
                .build();
        post.setPostDetails(details);

        // Comments 추가
        for (int i = 1; i <= 5; i++) {
            PostComment comment = PostComment.builder()
                    .review("댓글 " + i)
                    .build();
            post.addComment(comment);
        }

        // Tags 추가
        Tag hibernateTag = tagRepository.save(Tag.builder().name("Hibernate").build());
        Tag jpaTag = tagRepository.save(Tag.builder().name("JPA").build());
        Tag postgresqlTag = tagRepository.save(Tag.builder().name("PostgreSQL").build());
        post.addTag(hibernateTag);
        post.addTag(jpaTag);
        post.addTag(postgresqlTag);

        Post savedPost = postRepository.save(post);
        long saveEndTime = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] 복합 관계 Post 저장 완료 (저장 시간: {}ms)", saveEndTime - startTime);

        // 모든 연관 데이터를 한 번에 조회 (N+1 문제 해결)
        long fetchStartTime = System.currentTimeMillis();
        Optional<Post> fullPost = postRepository.findByIdWithAllAssociations(savedPost.getId());
        long fetchEndTime = System.currentTimeMillis();

        assertThat(fullPost).isPresent();
        assertThat(fullPost.get().getPostDetails()).isNotNull();
        assertThat(fullPost.get().getComments()).hasSize(5);
        assertThat(fullPost.get().getTags()).hasSize(3);
        log.info("[TMPFS-POSTGRESQL] JOIN FETCH로 모든 연관 데이터 조회 완료 (조회 시간: {}ms)", fetchEndTime - fetchStartTime);

        long endTime = System.currentTimeMillis();
        log.info("[TMPFS-POSTGRESQL] 복합 관계 테스트 총 시간: {}ms", endTime - startTime);
    }
}
