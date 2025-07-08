package com.side.springtestbed.hibernate.postgresql.controller;

import com.side.springtestbed.hibernate.postgresql.entity.Post;
import com.side.springtestbed.hibernate.postgresql.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hibernate/postgresql/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    
    private final PostService postService;
    
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        Post post = postService.createPost(request.getTitle(), request.getContent());
        return ResponseEntity.ok(post);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Post>> createBatchPosts(@RequestBody BatchCreateRequest request) {
        List<Post> posts = postService.createBatchPosts(request.getTitles(), request.getContent());
        return ResponseEntity.ok(posts);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request) {
        try {
            Post post = postService.updatePost(id, request.getTitle(), request.getContent());
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String title) {
        List<Post> posts = postService.searchPostsByTitle(title);
        return ResponseEntity.ok(posts);
    }
    
    // PostgreSQL 특화 엔드포인트들
    @GetMapping("/search/regex")
    public ResponseEntity<List<Post>> searchByRegex(@RequestParam String regex) {
        List<Post> posts = postService.searchByRegex(regex);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/search/fulltext")
    public ResponseEntity<List<Post>> fullTextSearch(@RequestParam String query) {
        List<Post> posts = postService.fullTextSearch(query);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/stats/recent")
    public ResponseEntity<Long> getRecentPostsCount(@RequestParam(defaultValue = "7") int days) {
        Long count = postService.getRecentPostsCount(days);
        return ResponseEntity.ok(count);
    }
    
    // Request DTOs
    public static class CreatePostRequest {
        private String title;
        private String content;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class BatchCreateRequest {
        private List<String> titles;
        private String content;
        
        public List<String> getTitles() { return titles; }
        public void setTitles(List<String> titles) { this.titles = titles; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class UpdatePostRequest {
        private String title;
        private String content;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
