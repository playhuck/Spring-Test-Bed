package com.side.springtestbed.hibernate.h2;

import com.side.springtestbed.common.utils.SQLStatementCountValidator;
import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
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
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("H2 인메모리 데이터베이스 통합테스트")
public class H2IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(H2IntegrationTest.class); // ★ 클래스명 수정

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("H2 인메모리 데이터베이스 연결 테스트")
    void testH2InMemoryConnection() {
        log.info("[H2-INMEMORY] 연결 테스트 시작");
        SQLStatementCountValidator.reset();

        // 간단한 연결 테스트
        long count = postRepository.count();
        assertThat(count).isEqualTo(0);

        log.info("탐색된 카운트 : {}", count);
    }

}