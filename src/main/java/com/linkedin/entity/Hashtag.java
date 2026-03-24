package com.linkedin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hashtags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String tag;
}
