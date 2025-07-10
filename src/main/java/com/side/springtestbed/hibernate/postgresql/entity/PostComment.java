package com.side.springtestbed.hibernate.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_comment_seq_gen") // Generator 이름 변경
    @SequenceGenerator(
            name = "post_comment_seq_gen", // Generator 이름 (GeneratedValue의 generator와 일치)
            sequenceName = "post_comment_id_seq", // 데이터베이스 시퀀스 이름 (고유하게 변경)
            allocationSize = 500 // 한 번에 1500개 ID를 미리 할당 받음
    )
    private Long id;
    
    @Column(length = 255)
    private String review;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    
    // Convenience constructor
    public PostComment(String review) {
        this.review = review;
    }
}
