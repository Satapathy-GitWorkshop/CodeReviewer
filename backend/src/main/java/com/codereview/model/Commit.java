package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(nullable = false, length = 40)
    private String sha;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private String author;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "commit_date")
    private LocalDateTime commitDate;

    @Column(name = "html_url")
    private String htmlUrl;

    @Column(name = "files_changed")
    private Integer filesChanged;

    @Column
    private Integer additions;

    @Column
    private Integer deletions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
