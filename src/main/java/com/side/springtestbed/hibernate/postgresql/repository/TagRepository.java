package com.side.springtestbed.hibernate.postgresql.repository;

import com.side.springtestbed.hibernate.postgresql.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    List<Tag> findByNameContaining(String name);
    
    @Query("SELECT t FROM Tag t JOIN FETCH t.posts WHERE t.id = :id")
    Optional<Tag> findByIdWithPosts(@Param("id") Long id);
    
    @Query(value = "SELECT t.* FROM tag t " +
                   "INNER JOIN post_tag pt ON t.id = pt.tag_id " +
                   "WHERE pt.post_id = :postId", nativeQuery = true)
    List<Tag> findByPostId(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_tag WHERE tag_id = :tagId", nativeQuery = true)
    Long countPostsByTagId(@Param("tagId") Long tagId);
}
