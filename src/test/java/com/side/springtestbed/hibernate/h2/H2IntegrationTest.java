package com.side.springtestbed.hibernate.h2;

import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.h2.datasource.url= jdbc:h2:tcp://localhost/~/test",
        "spring.h2.datasource.driver-class-name=org.h2.Driver",
        "spring.h2.datasource.username=sa",
        "spring.h2.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=true"
})
@DisplayName("H2 인메모리 데이터베이스 통합테스트")
public class H2IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(H2InMemoryIntegrationTest.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

}
