package com.codereview.scheduler;

import com.codereview.model.Repository;
import com.codereview.repository.RepositoryRepository;
import com.codereview.service.CommitAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisScheduler {

    private final RepositoryRepository repositoryRepository;
    private final CommitAnalysisService commitAnalysisService;

    // Run every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void runScheduledAnalysis() {
        log.info("Running scheduled analysis at {}", LocalDateTime.now());
        List<Repository> activeRepos = repositoryRepository.findByActiveTrue();

        for (Repository repo : activeRepos) {
            try {
                log.info("Analyzing repository: {}", repo.getFullName());
                commitAnalysisService.analyzeRepository(repo);
                repo.setLastAnalyzedAt(LocalDateTime.now());
                repositoryRepository.save(repo);
            } catch (Exception e) {
                log.error("Error analyzing repository {}: {}", repo.getFullName(), e.getMessage());
            }
        }

        log.info("Scheduled analysis complete. Processed {} repositories", activeRepos.size());
    }
}
