package com.codereview.controller;

import com.codereview.dto.ApiDtos;
import com.codereview.model.Repository;
import com.codereview.repository.RepositoryRepository;
import com.codereview.service.CommitAnalysisService;
import com.codereview.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {

    private final RepositoryService repositoryService;
    private final CommitAnalysisService commitAnalysisService;
    private final RepositoryRepository repositoryRepository;

    @GetMapping("/repositories")
    public ResponseEntity<List<ApiDtos.RepositoryDto>> getRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @PostMapping("/repositories")
    public ResponseEntity<ApiDtos.RepositoryDto> addRepository(@RequestBody ApiDtos.AddRepositoryDto request) {
        ApiDtos.RepositoryDto repo = repositoryService.addRepository(request.getOwner(), request.getName());
        return ResponseEntity.ok(repo);
    }

    @GetMapping("/commits")
    public ResponseEntity<List<ApiDtos.CommitDto>> getCommits(
            @RequestParam Long repoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(commitAnalysisService.getCommitsByRepository(repoId, since));
    }

    @GetMapping("/commits/{sha}/analysis")
    public ResponseEntity<ApiDtos.CommitDetailDto> getCommitAnalysis(@PathVariable String sha) {
        try {
            return ResponseEntity.ok(commitAnalysisService.getCommitDetail(sha));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/commits/{sha}/ai-summary")
    public ResponseEntity<?> getAiSummary(@PathVariable String sha) {
        return commitAnalysisService.getCommitDetail(sha).getAiSummary() != null
                ? ResponseEntity.ok(commitAnalysisService.getCommitDetail(sha).getAiSummary())
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/dashboard/metrics")
    public ResponseEntity<ApiDtos.DashboardMetricsDto> getDashboardMetrics(@RequestParam Long repoId) {
        return ResponseEntity.ok(commitAnalysisService.getDashboardMetrics(repoId));
    }

    @PostMapping("/analyze/manual")
    public ResponseEntity<Map<String, String>> manualAnalysis(@RequestBody ApiDtos.ManualAnalysisRequestDto request) {
        Repository repo = repositoryService.findById(request.getRepositoryId());
        new Thread(() -> {
            try {
                commitAnalysisService.analyzeRepository(repo);
                repo.setLastAnalyzedAt(LocalDateTime.now());
                repositoryRepository.save(repo);
            } catch (Exception e) {
                log.error("Error in manual analysis: {}", e.getMessage());
            }
        }).start();
        return ResponseEntity.ok(Map.of("status", "Analysis started for " + repo.getFullName()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", LocalDateTime.now().toString()));
    }
}
