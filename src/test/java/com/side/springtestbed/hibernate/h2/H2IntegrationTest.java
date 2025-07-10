package com.side.springtestbed.hibernate.h2;

import com.side.springtestbed.common.utils.SQLStatementCountValidator;
import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
// 이 임포트는 유지

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // 배치 설정 강화
        "spring.jpa.properties.hibernate.jdbc.batch_size=500",
        "spring.jpa.properties.hibernate.order_inserts=true",
        "spring.jpa.properties.hibernate.order_updates=true",
        "spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true",

        // H2용 ID 생성 전략 오버라이드
        "spring.jpa.properties.hibernate.id.new_generator_mappings=false"
})
@DisplayName("H2 인메모리 데이터베이스 통합테스트")
public class H2IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(H2IntegrationTest.class); // ★ 클래스명 수정

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DataSource dataSource;

    public H2IntegrationTest() {}

    @Test
    @DisplayName("H2 인메모리 데이터베이스 연결 테스트")
    void testH2InMemoryConnection() {
        log.info("H2-INMEMORY 연결 테스트 시작");
        SQLStatementCountValidator.reset();

        // 간단한 연결 테스트
        long count = postRepository.count();
        assertThat(count).isEqualTo(0);

        log.info("탐색된 카운트 : {}", count);
    }

    @Test
    @Transactional
    @DisplayName("Post CRUD TEST")
    void testInsertAndSelect() {

        log.info("CRUD Test 시작");

        Post post = Post
                .builder()
                .title("CRUD Title")
                .content("CRUD Content")
                .createdDate(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        assertThat(savedPost.getId()).isNotNull();
        entityManager.flush();

        Optional<Post> getPostById = postRepository.findById(savedPost.getId());
        assertThat(getPostById.isPresent()).isTrue();
        assertThat(getPostById.get().getTitle()).isEqualTo(post.getTitle());
        entityManager.flush();

        Post post2 = getPostById.get();
        post2.setTitle("Update");
        postRepository.save(post2);
        Optional<Post> getPostById2 = postRepository.findById(post2.getId());
        assertThat(getPostById2.isPresent()).isTrue();
        assertThat(getPostById2.get().getTitle()).isEqualTo(post2.getTitle());
        entityManager.flush();

        postRepository.delete(post);
        Optional<Post> getPostById3 = postRepository.findById(post.getId());
        assertThat(getPostById3.isPresent()).isFalse();
        entityManager.flush();

        log.info("CRUD Test 종료");

    }

    @Test
    @Transactional
    @DisplayName("H2 성능 테스트 - 대량 데이터 처리")
    void testH2PerformanceWithBulkData() {

        log.info("H2-INMEMORY Bulk 처리 시작");

        long startTime = System.currentTimeMillis();

        List<Post> postList = new ArrayList<>(500);

        // 대량 데이터 생성 (500개 포스트, 각각 3개 댓글)
        for (int i = 1; i <= 5000; i++) {
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
        entityManager.flush();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 결과 검증
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        assertThat(totalPosts).isEqualTo(5000);
        assertThat(totalComments).isEqualTo(5000);

        log.info("H2-INMEMORY 포스트 수: {}", totalPosts);
        log.info("H2-INMEMORY 댓글 수: {}", totalComments);
        log.info("H2-INMEMORY 총 처리 시간: {}ms", duration);

        long searchStartTime = System.currentTimeMillis();
        List<Post> searchResults = postRepository.findByTitleContaining("성능 테스트");
        entityManager.flush();
        long searchEndTime = System.currentTimeMillis();
        long searchDuration = searchEndTime - searchStartTime;

        assertThat(searchResults).hasSize(5000);
        log.info("H2-INMEMORY 대량 검색 테스트 완료: {} 건 조회 (검색 시간: {}ms)", searchResults.size(), searchDuration);

        log.info("H2-INMEMORY 성능 테스트 완료");


    }


}