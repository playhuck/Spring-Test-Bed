package com.side.springtestbed.hibernate.postgresql.repository;

import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    
    List<PostComment> findByPostId(Long postId);
    
    List<PostComment> findByReviewContaining(String review);
    
    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId ORDER BY c.id")
    List<PostComment> findByPostIdOrderById(@Param("postId") Long postId);
    
    @Query("SELECT c FROM PostComment c JOIN FETCH c.post WHERE c.id = :id")
    PostComment findByIdWithPost(@Param("id") Long id);
    
    @Query(value = "SELECT COUNT(*) FROM post_comment WHERE post_id = :postId", nativeQuery = true)
    Long countByPostId(@Param("postId") Long postId);
}
