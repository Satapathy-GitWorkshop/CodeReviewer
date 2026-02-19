package com.codereview.repository;

import com.codereview.model.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {
    Optional<AiSummary> findByCommitSha(String commitSha);
    List<AiSummary> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    @Query("SELECT AVG(a.riskScore) FROM AiSummary a WHERE a.repository.id = :repoId")
    Double findAvgRiskScoreByRepositoryId(@Param("repoId") Long repoId);

    @Query("SELECT COUNT(a) FROM AiSummary a WHERE a.repository.id = :repoId AND a.riskScore >= 7")
    long countHighRiskByRepositoryId(@Param("repoId") Long repoId);
}
