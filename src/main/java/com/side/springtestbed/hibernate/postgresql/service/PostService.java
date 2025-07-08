package com.side.springtestbed.hibernate.postgresql.service;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.entity.PostComment;
import com.side.springtestbed.hibernate.postgresql.entity.PostDetails;
import com.side.springtestbed.hibernate.postgresql.entity.Tag;
import com.side.springtestbed.hibernate.postgresql.repository.PostCommentRepository;
import com.side.springtestbed.hibernate.postgresql.repository.PostRepository;
import com.side.springtestbed.hibernate.postgresql.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final TagRepository tagRepository;
    
    @Transactional
    public Post createPost(String title, String content) {
        log.info("Creating new post: title={}, content={}", title, content);
        
        Post post = Post.builder()
                .title(title)
                .content(content)
                .createdDate(LocalDateTime.now())
                .build();
        
        Post savedPost = postRepository.save(post);
        log.info("Post created successfully: id={}", savedPost.getId());
        
        return savedPost;
    }
    
    @Transactional
    public List<Post> createBatchPosts(List<String> titles, String content) {
        log.info("Creating batch posts: titles count={}, content={}", titles.size(), content);
        
        List<Post> posts = new ArrayList<>();
        for (String title : titles) {
            Post post = Post.builder()
                    .title(title)
                    .content(content)
                    .createdDate(LocalDateTime.now())
                    .build();
            posts.add(post);
        }
        
        List<Post> savedPosts = postRepository.saveAll(posts);
        log.info("Batch posts created successfully: count={}", savedPosts.size());
        
        return savedPosts;
    }
    
    @Transactional
    public Post updatePost(Long id, String title, String content) {
        log.info("Updating post: id={}, title={}, content={}", id, title, content);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        post.setTitle(title);
        post.setContent(content);
        
        Post updatedPost = postRepository.save(post);
        log.info("Post updated successfully: id={}", updatedPost.getId());
        
        return updatedPost;
    }
    
    @Transactional(readOnly = true)
    public List<Post> searchPostsByTitle(String title) {
        log.info("Searching posts by title: {}", title);
        return postRepository.findByTitleContainingIgnoreCase(title);
    }
    
    @Transactional(readOnly = true)
    public List<Post> searchByRegex(String regex) {
        log.info("Searching posts by regex: {}", regex);
        return postRepository.findByTitleRegex(regex);
    }
    
    @Transactional(readOnly = true)
    public List<Post> fullTextSearch(String query) {
        log.info("Full text search: {}", query);
        return postRepository.fullTextSearch(query);
    }
    
    @Transactional(readOnly = true)
    public Long getRecentPostsCount(int days) {
        log.info("Getting recent posts count for {} days", days);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return postRepository.countByCreatedDateAfter(cutoffDate);
    }
    
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        log.info("Fetching all posts");
        return postRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Post> getPostById(Long id) {
        log.info("Fetching post by id: {}", id);
        return postRepository.findById(id);
    }
    
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting post: id={}", id);
        postRepository.deleteById(id);
        log.info("Post deleted successfully: id={}", id);
    }
}
