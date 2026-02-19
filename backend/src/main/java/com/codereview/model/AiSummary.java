package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String improvements;

    @Column(name = "security_concerns", columnDefinition = "TEXT")
    private String securityConcerns;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
