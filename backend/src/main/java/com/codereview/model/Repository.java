package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "repositories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "last_analyzed_at")
    private LocalDateTime lastAnalyzedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "default_branch")
    private String defaultBranch = "main";

    @Column
    private String language;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
