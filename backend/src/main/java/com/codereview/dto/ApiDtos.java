package com.codereview.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ApiDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RepositoryDto {
        private Long id;
        private String owner;
        private String name;
        private String fullName;
        private boolean active;
        private LocalDateTime lastAnalyzedAt;
        private String language;
        private String defaultBranch;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommitDto {
        private Long id;
        private String sha;
        private String message;
        private String author;
        private String authorEmail;
        private LocalDateTime commitDate;
        private String htmlUrl;
        private Integer filesChanged;
        private Integer additions;
        private Integer deletions;
        private Long repositoryId;
        private String repositoryName;
        private Integer riskScore;
        private String riskLevel;
        private String aiSummaryPreview;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AnalysisResultDto {
        private Long id;
        private String commitSha;
        private String language;
        private String toolName;
        private String issues;
        private Integer issueCount;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AiSummaryDto {
        private Long id;
        private String commitSha;
        private String summary;
        private Integer riskScore;
        private String riskLevel;
        private String improvements;
        private String securityConcerns;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardMetricsDto {
        private Long repositoryId;
        private String repositoryName;
        private long totalCommits;
        private Double avgRiskScore;
        private long highRiskCount;
        private long mediumRiskCount;
        private long lowRiskCount;
        private List<RiskTrendPoint> riskTrend;
        private List<CommitDto> recentCommits;
        private String primaryLanguage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskTrendPoint {
        private String date;
        private Double riskScore;
        private String commitSha;
        private String commitMessage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommitDetailDto {
        private CommitDto commit;
        private List<AnalysisResultDto> analysisResults;
        private AiSummaryDto aiSummary;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ManualAnalysisRequestDto {
        private Long repositoryId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddRepositoryDto {
        private String owner;
        private String name;
    }
}
