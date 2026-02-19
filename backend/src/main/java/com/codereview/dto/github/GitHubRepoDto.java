package com.codereview.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubRepoDto {
    private Long id;
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;
    private String language;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    private Owner owner;

    @Data
    public static class Owner {
        private String login;
        private String type;
    }
}
