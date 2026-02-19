package com.codereview.service;

import com.codereview.client.GitHubClient;
import com.codereview.dto.ApiDtos;
import com.codereview.dto.github.GitHubRepoDto;
import com.codereview.model.Repository;
import com.codereview.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final GitHubClient gitHubClient;

    public List<ApiDtos.RepositoryDto> getAllRepositories() {
        return repositoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ApiDtos.RepositoryDto> getActiveRepositories() {
        return repositoryRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ApiDtos.RepositoryDto addRepository(String owner, String name) {
        String fullName = owner + "/" + name;
        return repositoryRepository.findByFullName(fullName)
                .map(this::toDto)
                .orElseGet(() -> {
                    try {
                        GitHubRepoDto ghRepo = gitHubClient.getRepository(owner, name);
                        Repository repo = Repository.builder()
                                .owner(owner)
                                .name(name)
                                .fullName(fullName)
                                .active(true)
                                .language(ghRepo.getLanguage())
                                .defaultBranch(ghRepo.getDefaultBranch() != null ? ghRepo.getDefaultBranch() : "main")
                                .build();
                        return toDto(repositoryRepository.save(repo));
                    } catch (Exception e) {
                        log.warn("Could not fetch repo from GitHub, adding with minimal info: {}", e.getMessage());
                        Repository repo = Repository.builder()
                                .owner(owner)
                                .name(name)
                                .fullName(fullName)
                                .active(true)
                                .defaultBranch("main")
                                .build();
                        return toDto(repositoryRepository.save(repo));
                    }
                });
    }

    public Repository findById(Long id) {
        return repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found: " + id));
    }

    public void save(Repository repository) {
        repositoryRepository.save(repository);
    }

    private ApiDtos.RepositoryDto toDto(Repository repo) {
        return ApiDtos.RepositoryDto.builder()
                .id(repo.getId())
                .owner(repo.getOwner())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .active(repo.isActive())
                .lastAnalyzedAt(repo.getLastAnalyzedAt())
                .language(repo.getLanguage())
                .defaultBranch(repo.getDefaultBranch())
                .build();
    }
}
