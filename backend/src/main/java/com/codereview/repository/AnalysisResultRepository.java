package com.codereview.repository;

import com.codereview.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    List<AnalysisResult> findByCommitSha(String commitSha);
    List<AnalysisResult> findByRepositoryId(Long repositoryId);
    boolean existsByCommitShaAndToolName(String commitSha, String toolName);
}
