package com.codereview.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class GitHubCommitDto {
    private String sha;
    private CommitDetail commit;
    private String url;

    @JsonProperty("html_url")
    private String htmlUrl;

    private List<FileChange> files;
    private Stats stats;

    @Data
    public static class CommitDetail {
        private String message;
        private Author author;
        private Author committer;
    }

    @Data
    public static class Author {
        private String name;
        private String email;
        private String date;
    }

    @Data
    public static class FileChange {
        private String filename;
        private String status;
        private Integer additions;
        private Integer deletions;
        private Integer changes;
        private String patch;

        @JsonProperty("blob_url")
        private String blobUrl;
    }

    @Data
    public static class Stats {
        private Integer total;
        private Integer additions;
        private Integer deletions;
    }
}
