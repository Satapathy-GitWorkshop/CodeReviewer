package com.codereview.service;

import com.codereview.client.GitHubClient;
import com.codereview.dto.ApiDtos;
import com.codereview.dto.github.GitHubCommitDto;
import com.codereview.model.*;
import com.codereview.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommitAnalysisService {

    private final GitHubClient gitHubClient;
    private final CommitRepository commitRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final StaticAnalysisService staticAnalysisService;
    private final AiSummaryService aiSummaryService;

    public void analyzeRepository(Repository repository) {
        log.info("Starting analysis for repository: {}", repository.getFullName());

        LocalDateTime since = repository.getLastAnalyzedAt() != null
                ? repository.getLastAnalyzedAt().minusHours(1)
                : LocalDateTime.now().minusDays(30);

        List<GitHubCommitDto> ghCommits = gitHubClient.getCommits(
                repository.getOwner(), repository.getName(), since, 20);

        log.info("Fetched {} commits for {}", ghCommits.size(), repository.getFullName());

        for (GitHubCommitDto ghCommit : ghCommits) {
            try {
                processCommit(repository, ghCommit);
                // Small delay to respect rate limits
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Error processing commit {}: {}", ghCommit.getSha(), e.getMessage());
            }
        }
    }

    private void processCommit(Repository repository, GitHubCommitDto ghCommit) throws InterruptedException {
        if (commitRepository.existsBySha(ghCommit.getSha())) {
            log.debug("Commit {} already exists, checking for AI summary", ghCommit.getSha());
            // Still generate AI summary if missing
            if (!aiSummaryRepository.findByCommitSha(ghCommit.getSha()).isPresent()) {
                generateAiForExistingCommit(repository, ghCommit.getSha());
            }
            return;
        }

        // Fetch full commit detail for diff
        GitHubCommitDto detail = gitHubClient.getCommitDetail(
                repository.getOwner(), repository.getName(), ghCommit.getSha());
        if (detail == null) detail = ghCommit;

        // Save commit
        Commit commit = saveCommit(repository, detail);

        // Run static analysis
        List<AnalysisResult> analysisResults = staticAnalysisService.analyzeCommit(
                repository, ghCommit.getSha(), detail.getFiles());

        // Build diff string
        String diff = buildDiff(detail.getFiles());

        // Generate AI summary
        aiSummaryService.generateAndSaveSummary(
                repository, ghCommit.getSha(),
                ghCommit.getCommit().getMessage(), diff, analysisResults);
    }

    private void generateAiForExistingCommit(Repository repository, String sha) {
        GitHubCommitDto detail = gitHubClient.getCommitDetail(
                repository.getOwner(), repository.getName(), sha);
        if (detail != null) {
            String diff = buildDiff(detail.getFiles());
            List<AnalysisResult> analysisResults = analysisResultRepository.findByCommitSha(sha);
            aiSummaryService.generateAndSaveSummary(
                    repository, sha, detail.getCommit().getMessage(), diff, analysisResults);
        }
    }

    private Commit saveCommit(Repository repository, GitHubCommitDto detail) {
        GitHubCommitDto.CommitDetail cd = detail.getCommit();
        LocalDateTime commitDate = null;
        if (cd.getAuthor() != null && cd.getAuthor().getDate() != null) {
            try {
                commitDate = LocalDateTime.parse(cd.getAuthor().getDate(),
                        DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                commitDate = LocalDateTime.now();
            }
        }

        Commit commit = Commit.builder()
                .repository(repository)
                .sha(detail.getSha())
                .message(cd.getMessage())
                .author(cd.getAuthor() != null ? cd.getAuthor().getName() : "Unknown")
                .authorEmail(cd.getAuthor() != null ? cd.getAuthor().getEmail() : "")
                .commitDate(commitDate)
                .htmlUrl(detail.getHtmlUrl())
                .filesChanged(detail.getFiles() != null ? detail.getFiles().size() : 0)
                .additions(detail.getStats() != null ? detail.getStats().getAdditions() : 0)
                .deletions(detail.getStats() != null ? detail.getStats().getDeletions() : 0)
                .build();

        return commitRepository.save(commit);
    }

    private String buildDiff(List<GitHubCommitDto.FileChange> files) {
        if (files == null) return "";
        StringBuilder sb = new StringBuilder();
        for (GitHubCommitDto.FileChange f : files) {
            sb.append("File: ").append(f.getFilename()).append("\n");
            if (f.getPatch() != null) {
                sb.append(f.getPatch(), 0, Math.min(f.getPatch().length(), 1000)).append("\n");
            }
        }
        return sb.toString();
    }

    public List<ApiDtos.CommitDto> getCommitsByRepository(Long repoId, LocalDateTime since) {
        List<Commit> commits = since != null
                ? commitRepository.findByRepositoryIdAndSince(repoId, since)
                : commitRepository.findByRepositoryIdOrderByCommitDateDesc(repoId);

        return commits.stream().map(c -> enrichCommitDto(c)).collect(Collectors.toList());
    }

    private ApiDtos.CommitDto enrichCommitDto(Commit commit) {
        ApiDtos.CommitDto dto = ApiDtos.CommitDto.builder()
                .id(commit.getId())
                .sha(commit.getSha())
                .message(commit.getMessage())
                .author(commit.getAuthor())
                .authorEmail(commit.getAuthorEmail())
                .commitDate(commit.getCommitDate())
                .htmlUrl(commit.getHtmlUrl())
                .filesChanged(commit.getFilesChanged())
                .additions(commit.getAdditions())
                .deletions(commit.getDeletions())
                .repositoryId(commit.getRepository().getId())
                .repositoryName(commit.getRepository().getFullName())
                .build();

        aiSummaryRepository.findByCommitSha(commit.getSha()).ifPresent(ai -> {
            dto.setRiskScore(ai.getRiskScore());
            dto.setRiskLevel(ai.getRiskLevel());
            String summary = ai.getSummary();
            dto.setAiSummaryPreview(summary != null && summary.length() > 120
                    ? summary.substring(0, 120) + "..." : summary);
        });

        return dto;
    }

    public ApiDtos.CommitDetailDto getCommitDetail(String sha) {
        Commit commit = commitRepository.findBySha(sha)
                .orElseThrow(() -> new RuntimeException("Commit not found: " + sha));

        List<ApiDtos.AnalysisResultDto> analysisResults = analysisResultRepository.findByCommitSha(sha)
                .stream().map(r -> ApiDtos.AnalysisResultDto.builder()
                        .id(r.getId())
                        .commitSha(r.getCommitSha())
                        .language(r.getLanguage())
                        .toolName(r.getToolName())
                        .issues(r.getIssues())
                        .issueCount(r.getIssueCount())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        ApiDtos.AiSummaryDto aiSummary = aiSummaryRepository.findByCommitSha(sha)
                .map(aiSummaryService::toDto)
                .orElse(null);

        return ApiDtos.CommitDetailDto.builder()
                .commit(enrichCommitDto(commit))
                .analysisResults(analysisResults)
                .aiSummary(aiSummary)
                .build();
    }

    public ApiDtos.DashboardMetricsDto getDashboardMetrics(Long repoId) {
        var commits = commitRepository.findByRepositoryIdOrderByCommitDateDesc(repoId);
        var summaries = aiSummaryRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId);

        long total = commits.size();
        Double avgRisk = aiSummaryRepository.findAvgRiskScoreByRepositoryId(repoId);
        long highRisk = aiSummaryRepository.countHighRiskByRepositoryId(repoId);

        long mediumRisk = summaries.stream().filter(s -> s.getRiskScore() != null && s.getRiskScore() >= 4 && s.getRiskScore() < 7).count();
        long lowRisk = summaries.stream().filter(s -> s.getRiskScore() != null && s.getRiskScore() < 4).count();

        // Build risk trend from summaries
        List<ApiDtos.RiskTrendPoint> trend = summaries.stream()
                .limit(30)
                .map(s -> {
                    Optional<Commit> c = commitRepository.findBySha(s.getCommitSha());
                    return ApiDtos.RiskTrendPoint.builder()
                            .date(s.getCreatedAt() != null ? s.getCreatedAt().toString().substring(0, 10) : "")
                            .riskScore(s.getRiskScore() != null ? s.getRiskScore().doubleValue() : 0)
                            .commitSha(s.getCommitSha())
                            .commitMessage(c.map(cm -> cm.getMessage() != null && cm.getMessage().length() > 60
                                    ? cm.getMessage().substring(0, 60) : cm.getMessage()).orElse(""))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(trend);

        List<ApiDtos.CommitDto> recentCommits = commits.stream()
                .limit(10)
                .map(this::enrichCommitDto)
                .collect(Collectors.toList());

        Repository repo = commits.isEmpty() ? null : commits.get(0).getRepository();

        return ApiDtos.DashboardMetricsDto.builder()
                .repositoryId(repoId)
                .repositoryName(repo != null ? repo.getFullName() : "")
                .totalCommits(total)
                .avgRiskScore(avgRisk != null ? Math.round(avgRisk * 10.0) / 10.0 : 0.0)
                .highRiskCount(highRisk)
                .mediumRiskCount(mediumRisk)
                .lowRiskCount(lowRisk)
                .riskTrend(trend)
                .recentCommits(recentCommits)
                .primaryLanguage(repo != null ? repo.getLanguage() : "Unknown")
                .build();
    }
}
