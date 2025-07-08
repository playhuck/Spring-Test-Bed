package com.side.springtestbed.hibernate.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();
    
    // Convenience constructor
    public Tag(String name) {
        this.name = name;
    }
    
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
