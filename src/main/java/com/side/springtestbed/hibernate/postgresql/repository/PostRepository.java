package com.side.springtestbed.hibernate.postgresql.repository;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByTitleContaining(String title);
    
    List<Post> findByTitleContainingIgnoreCase(String title);
    
    Long countByCreatedDateAfter(LocalDateTime date);
    
    @Query("SELECT p FROM Post p WHERE p.title = :title ORDER BY p.id DESC")
    List<Post> findByTitleOrderById(@Param("title") String title);
    
    // JOIN FETCH로 연관 데이터 함께 조회 (N+1 문제 해결)
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    Optional<Post> findByIdWithComments(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postDetails WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
    
    // 모든 연관 데이터 함께 조회
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.comments " +
           "LEFT JOIN FETCH p.tags " +
           "LEFT JOIN FETCH p.postDetails " +
           "WHERE p.id = :id")
    Optional<Post> findByIdWithAllAssociations(@Param("id") Long id);
    
    // PostgreSQL 특화 쿼리들
    @Query(value = "SELECT * FROM post WHERE title ~ :regex", nativeQuery = true)
    List<Post> findByTitleRegex(@Param("regex") String regex);
    
    @Query(value = "SELECT * FROM post WHERE to_tsvector('english', title || ' ' || coalesce(content, '')) @@ plainto_tsquery('english', :query)", nativeQuery = true)
    List<Post> fullTextSearch(@Param("query") String query);
    
    @Query(value = "SELECT p.* FROM post p " +
                   "INNER JOIN post_comment pc ON p.id = pc.post_id " +
                   "WHERE pc.review ILIKE %:keyword%", nativeQuery = true)
    List<Post> findByCommentReviewContaining(@Param("keyword") String keyword);
    
    @Query(value = "SELECT p.* FROM post p " +
                   "INNER JOIN post_tag pt ON p.id = pt.post_id " +
                   "INNER JOIN tag t ON pt.tag_id = t.id " +
                   "WHERE t.name = :tagName", nativeQuery = true)
    List<Post> findByTagName(@Param("tagName") String tagName);
    
    @Query(value = "SELECT COUNT(*) FROM post_comment WHERE post_id = :postId", nativeQuery = true)
    Long countCommentsByPostId(@Param("postId") Long postId);
}
