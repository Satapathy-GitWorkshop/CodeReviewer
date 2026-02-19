package com.codereview.repository;

import com.codereview.model.Commit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommitRepository extends JpaRepository<Commit, Long> {
    Optional<Commit> findBySha(String sha);
    boolean existsBySha(String sha);
    List<Commit> findByRepositoryIdOrderByCommitDateDesc(Long repositoryId);
    Page<Commit> findByRepositoryIdOrderByCommitDateDesc(Long repositoryId, Pageable pageable);

    @Query("SELECT c FROM Commit c WHERE c.repository.id = :repoId AND c.commitDate >= :since ORDER BY c.commitDate DESC")
    List<Commit> findByRepositoryIdAndSince(@Param("repoId") Long repoId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Commit c WHERE c.repository.id = :repoId")
    long countByRepositoryId(@Param("repoId") Long repoId);

    @Query("SELECT c FROM Commit c WHERE c.repository.id = :repoId ORDER BY c.commitDate DESC")
    List<Commit> findTop10ByRepositoryId(@Param("repoId") Long repoId, Pageable pageable);
}
