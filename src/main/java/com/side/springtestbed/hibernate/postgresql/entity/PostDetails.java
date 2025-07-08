package com.side.springtestbed.hibernate.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetails {
    
    @Id
    private Long id;
    
    @Column(name = "created_by", length = 255)
    private String createdBy;
    
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private Post post;
    
    @PrePersist
    protected void onCreate() {
        if (createdOn == null) {
            createdOn = LocalDateTime.now();
        }
    }
    
    // Convenience constructor
    public PostDetails(String createdBy) {
        this.createdBy = createdBy;
        this.createdOn = LocalDateTime.now();
    }
}
