package com.codereview.client;

import com.codereview.dto.github.GitHubCommitDto;
import com.codereview.dto.github.GitHubRepoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class GitHubClient {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final RestTemplate restTemplate;
    private final String githubToken;

    public GitHubClient(
            RestTemplate restTemplate,
            @Value("${github.token:}") String githubToken) {
        this.restTemplate = restTemplate;
        this.githubToken = githubToken;
    }

    public GitHubRepoDto getRepository(String owner, String repo) {
        String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo;
        try {
            ResponseEntity<GitHubRepoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, createRequestEntity(), GitHubRepoDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error fetching repository {}/{}: {}", owner, repo, e.getMessage());
            throw new RuntimeException("Failed to fetch repository: " + e.getMessage());
        }
    }

    public List<GitHubCommitDto> getCommits(String owner, String repo, LocalDateTime since, int perPage) {
        String url = UriComponentsBuilder.fromHttpUrl(GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/commits")
                .queryParam("per_page", perPage)
                .queryParam("since", since != null ? since.format(ISO_FORMATTER) + "Z" : null)
                .build()
                .toUriString();

        try {
            ResponseEntity<GitHubCommitDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, createRequestEntity(), GitHubCommitDto[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("Error fetching commits for {}/{}: {}", owner, repo, e.getMessage());
            return Collections.emptyList();
        }
    }

    public GitHubCommitDto getCommitDetail(String owner, String repo, String sha) {
        String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/commits/" + sha;
        try {
            ResponseEntity<GitHubCommitDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, createRequestEntity(), GitHubCommitDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error fetching commit detail {}: {}", sha, e.getMessage());
            return null;
        }
    }

    private HttpEntity<Void> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github.v3+json")));
        if (githubToken != null && !githubToken.isEmpty()) {
            headers.setBearerAuth(githubToken);
        }
        return new HttpEntity<>(headers);
    }
}
