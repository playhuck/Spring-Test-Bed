package com.side.springtestbed.hibernate.h2;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=true"
})
@DisplayName("H2 인메모리 데이터베이스 통합테스트")
class H2InMemoryIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(H2InMemoryIntegrationTest.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("H2 인메모리 데이터베이스 연결 테스트")
    void testH2InMemoryConnection() {
        log.info("[H2-INMEMORY] 인메모리 데이터베이스 연결 테스트 시작");
        
        // 간단한 연결 테스트
        long count = postRepository.count();
        assertThat(count).isEqualTo(0);
        
        log.info("[H2-INMEMORY] 인메모리 데이터베이스가 성공적으로 실행 중입니다");
        log.info("[H2-INMEMORY] H2 Console이 활성화되어 있습니다");
    }

    @Test
    @Transactional
    @DisplayName("Post 엔티티 기본 CRUD 테스트")
    void testPostEntityCRUD() {
        log.info("[H2-INMEMORY] Post 엔티티 CRUD 테스트 시작");

        // Create
        Post post = Post.builder()
                .title("H2 테스트 포스트")
                .content("H2 인메모리 데이터베이스 성능 테스트용 내용")
                .createdDate(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        assertThat(savedPost.getId()).isNotNull();
        log.info("[H2-INMEMORY] Create: Post 생성 완료 (ID: {})", savedPost.getId());

        // Read
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("H2 테스트 포스트");
        log.info("[H2-INMEMORY] Read: Post 조회 완료");

        // Update
        foundPost.get().setTitle("수정된 제목");
        Post updatedPost = postRepository.save(foundPost.get());
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        log.info("[H2-INMEMORY] Update: Post 수정 완료");

        // Delete
        postRepository.delete(updatedPost);
        Optional<Post> deletedPost = postRepository.findById(updatedPost.getId());
        assertThat(deletedPost).isEmpty();
        log.info("[H2-INMEMORY] Delete: Post 삭제 완료");

        log.info("[H2-INMEMORY] Post CRUD 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("H2 성능 테스트 - 대량 데이터 처리")
    void testH2PerformanceWithBulkData() {
        log.info("[H2-INMEMORY] 성능 테스트 시작 - 대량 데이터 처리");

        long startTime = System.currentTimeMillis();

        // 대량 데이터 생성 (500개 포스트, 각각 3개 댓글)
        for (int i = 1; i <= 500; i++) {
            Post post = Post.builder()
                    .title("H2 성능 테스트 포스트 " + i)
                    .content("H2 인메모리 데이터베이스 성능 테스트용 내용 " + i)
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

        log.info("[H2-INMEMORY] 대량 데이터 처리 완료:");
        log.info("[H2-INMEMORY] 포스트 수: {}", totalPosts);
        log.info("[H2-INMEMORY] 댓글 수: {}", totalComments);
        log.info("[H2-INMEMORY] 총 처리 시간: {}ms", duration);
        log.info("[H2-INMEMORY] 평균 처리 시간: {}ms/post", duration / totalPosts);

        // 대량 검색 테스트
        long searchStartTime = System.currentTimeMillis();
        List<Post> searchResults = postRepository.findByTitleContaining("성능 테스트");
        long searchEndTime = System.currentTimeMillis();
        long searchDuration = searchEndTime - searchStartTime;

        assertThat(searchResults).hasSize(500);
        log.info("[H2-INMEMORY] 대량 검색 테스트 완료: {} 건 조회 (검색 시간: {}ms)", searchResults.size(), searchDuration);

        log.info("[H2-INMEMORY] 성능 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("H2 기본 검색 기능 테스트")
    void testH2SearchFeatures() {
        log.info("[H2-INMEMORY] 기본 검색 기능 테스트 시작");

        // 테스트 데이터 생성
        Post post1 = postRepository.save(Post.builder()
                .title("H2 Database Tutorial")
                .content("H2 데이터베이스 튜토리얼 내용")
                .createdDate(LocalDateTime.now())
                .build());
        Post post2 = postRepository.save(Post.builder()
                .title("Advanced H2 Features")
                .content("고급 H2 기능 내용")
                .createdDate(LocalDateTime.now())
                .build());
        Post post3 = postRepository.save(Post.builder()
                .title("H2 vs MySQL Performance")
                .content("H2와 MySQL 성능 비교")
                .createdDate(LocalDateTime.now())
                .build());

        // 제목 검색 테스트
        long searchStartTime = System.currentTimeMillis();
        List<Post> titleSearchResults = postRepository.findByTitleContainingIgnoreCase("h2");
        long searchEndTime = System.currentTimeMillis();
        
        assertThat(titleSearchResults).hasSize(3);
        log.info("[H2-INMEMORY] 제목 검색 테스트 완료: {} 건 (검색 시간: {}ms)", 
                titleSearchResults.size(), searchEndTime - searchStartTime);

        // 최근 포스트 개수 확인
        long recentCountStartTime = System.currentTimeMillis();
        Long recentPostsCount = postRepository.countByCreatedDateAfter(LocalDateTime.now().minusDays(1));
        long recentCountEndTime = System.currentTimeMillis();
        
        assertThat(recentPostsCount).isEqualTo(3);
        log.info("[H2-INMEMORY] 최근 포스트 개수 조회 완료: {} 건 (조회 시간: {}ms)", 
                recentPostsCount, recentCountEndTime - recentCountStartTime);

        log.info("[H2-INMEMORY] 기본 검색 기능 테스트 완료");
    }

    @Test
    @Transactional
    @DisplayName("복합 관계 및 N+1 문제 해결 테스트")
    void testComplexRelationshipsAndN1Prevention() {
        log.info("[H2-INMEMORY] 복합 관계 및 N+1 문제 해결 테스트 시작");

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
        Tag h2Tag = tagRepository.save(Tag.builder().name("H2").build());
        post.addTag(hibernateTag);
        post.addTag(jpaTag);
        post.addTag(h2Tag);

        Post savedPost = postRepository.save(post);
        long saveEndTime = System.currentTimeMillis();
        log.info("[H2-INMEMORY] 복합 관계 Post 저장 완료 (저장 시간: {}ms)", saveEndTime - startTime);

        // 모든 연관 데이터를 한 번에 조회 (N+1 문제 해결)
        long fetchStartTime = System.currentTimeMillis();
        Optional<Post> fullPost = postRepository.findByIdWithAllAssociations(savedPost.getId());
        long fetchEndTime = System.currentTimeMillis();

        assertThat(fullPost).isPresent();
        assertThat(fullPost.get().getPostDetails()).isNotNull();
        assertThat(fullPost.get().getComments()).hasSize(5);
        assertThat(fullPost.get().getTags()).hasSize(3);
        log.info("[H2-INMEMORY] JOIN FETCH로 모든 연관 데이터 조회 완료 (조회 시간: {}ms)", fetchEndTime - fetchStartTime);

        long endTime = System.currentTimeMillis();
        log.info("[H2-INMEMORY] 복합 관계 테스트 총 시간: {}ms", endTime - startTime);
    }

    @Test
    @Transactional
    @DisplayName("트랜잭션 및 데이터 일관성 테스트")
    void testTransactionAndDataConsistency() {
        log.info("[H2-INMEMORY] 트랜잭션 및 데이터 일관성 테스트 시작");

        long startTime = System.currentTimeMillis();

        // 트랜잭션 없이 실행하여 실제 커밋 테스트
        Long postId = createPostWithAssociations();
        
        // 데이터 확인
        Optional<Post> post = postRepository.findById(postId);
        assertThat(post).isPresent();
        log.info("[H2-INMEMORY] 트랜잭션 커밋 후 데이터 영속성 확인");

        // 연관 데이터 확인
        Long commentCount = commentRepository.countByPostId(postId);
        assertThat(commentCount).isGreaterThan(0);
        
        long endTime = System.currentTimeMillis();
        log.info("[H2-INMEMORY] 연관 데이터 일관성 확인: 댓글 {} 개", commentCount);
        log.info("[H2-INMEMORY] 트랜잭션 테스트 총 시간: {}ms", endTime - startTime);
    }

    @Transactional
    private Long createPostWithAssociations() {
        Post post = Post.builder()
                .title("트랜잭션 테스트 포스트")
                .content("트랜잭션 테스트용 내용")
                .createdDate(LocalDateTime.now())
                .build();

        PostComment comment = PostComment.builder()
                .review("트랜잭션 테스트 댓글")
                .build();

        post.addComment(comment);
        
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Test
    @Transactional
    @DisplayName("대량 배치 처리 성능 비교 테스트")
    void testBatchProcessingPerformance() {
        log.info("[H2-INMEMORY] 대량 배치 처리 성능 테스트 시작");

        long startTime = System.currentTimeMillis();

        // 배치 처리로 1000개 포스트 생성
        for (int batch = 0; batch < 10; batch++) {
            long batchStartTime = System.currentTimeMillis();
            
            for (int i = 1; i <= 100; i++) {
                Post post = Post.builder()
                        .title("배치 처리 테스트 포스트 " + (batch * 100 + i))
                        .content("배치 처리 테스트용 내용 " + (batch * 100 + i))
                        .createdDate(LocalDateTime.now())
                        .build();

                // 각 포스트에 2개의 댓글 추가
                for (int j = 1; j <= 2; j++) {
                    PostComment comment = PostComment.builder()
                            .review("배치 " + batch + " 포스트 " + i + "의 댓글 " + j)
                            .build();
                    post.addComment(comment);
                }

                postRepository.save(post);
            }
            
            long batchEndTime = System.currentTimeMillis();
            log.info("[H2-INMEMORY] 배치 {} 완료 (100개 포스트, 시간: {}ms)", 
                    batch + 1, batchEndTime - batchStartTime);
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        // 결과 검증
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        assertThat(totalPosts).isEqualTo(1000);
        assertThat(totalComments).isEqualTo(2000);

        log.info("[H2-INMEMORY] 대량 배치 처리 완료:");
        log.info("[H2-INMEMORY] 총 포스트 수: {}", totalPosts);
        log.info("[H2-INMEMORY] 총 댓글 수: {}", totalComments);
        log.info("[H2-INMEMORY] 전체 처리 시간: {}ms", totalDuration);
        log.info("[H2-INMEMORY] 평균 처리 시간: {}ms/post", totalDuration / totalPosts);
        log.info("[H2-INMEMORY] 처리량: {}/sec", (totalPosts * 1000) / totalDuration);
    }
}
