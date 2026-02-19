package com.codereview.service;

import com.codereview.dto.github.GitHubCommitDto;
import com.codereview.model.AnalysisResult;
import com.codereview.model.Repository;
import com.codereview.repository.AnalysisResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaticAnalysisService {

    private final AnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper;

    /**
     * Performs static analysis on commit files.
     * In a real deployment, this would invoke PMD, SpotBugs, Pylint, Bandit, etc.
     * Here we do pattern-based analysis on the diff content.
     */
    public List<AnalysisResult> analyzeCommit(Repository repository, String commitSha,
                                               List<GitHubCommitDto.FileChange> files) {
        List<AnalysisResult> results = new ArrayList<>();

        if (files == null || files.isEmpty()) return results;

        // Group files by language
        Map<String, List<GitHubCommitDto.FileChange>> byLanguage = groupFilesByLanguage(files);

        for (Map.Entry<String, List<GitHubCommitDto.FileChange>> entry : byLanguage.entrySet()) {
            String language = entry.getKey();
            List<GitHubCommitDto.FileChange> langFiles = entry.getValue();

            // Skip if already analyzed
            String toolName = getToolName(language);
            if (analysisResultRepository.existsByCommitShaAndToolName(commitSha, toolName)) {
                log.debug("Analysis already exists for {} with tool {}", commitSha, toolName);
                continue;
            }

            List<Map<String, Object>> issues = performPatternAnalysis(langFiles, language);

            AnalysisResult result = AnalysisResult.builder()
                    .repository(repository)
                    .commitSha(commitSha)
                    .language(language)
                    .toolName(toolName)
                    .issueCount(issues.size())
                    .issues(toJson(issues))
                    .build();

            results.add(analysisResultRepository.save(result));
        }

        return results;
    }

    private Map<String, List<GitHubCommitDto.FileChange>> groupFilesByLanguage(
            List<GitHubCommitDto.FileChange> files) {
        return files.stream().collect(Collectors.groupingBy(f -> detectLanguage(f.getFilename())));
    }

    private String detectLanguage(String filename) {
        if (filename == null) return "unknown";
        if (filename.endsWith(".java")) return "Java";
        if (filename.endsWith(".py")) return "Python";
        if (filename.endsWith(".js") || filename.endsWith(".ts") || filename.endsWith(".jsx") || filename.endsWith(".tsx")) return "JavaScript";
        if (filename.endsWith(".go")) return "Go";
        if (filename.endsWith(".rb")) return "Ruby";
        return "Other";
    }

    private String getToolName(String language) {
        return switch (language) {
            case "Java" -> "PMD+SpotBugs";
            case "Python" -> "Pylint+Bandit";
            case "JavaScript" -> "ESLint";
            default -> "Generic-Analysis";
        };
    }

    private List<Map<String, Object>> performPatternAnalysis(
            List<GitHubCommitDto.FileChange> files, String language) {
        List<Map<String, Object>> issues = new ArrayList<>();

        for (GitHubCommitDto.FileChange file : files) {
            if (file.getPatch() == null) continue;
            String patch = file.getPatch();

            // Common patterns across languages
            checkPattern(issues, file.getFilename(), patch, "TODO|FIXME|HACK|XXX",
                    "Code Quality", "Contains TODO/FIXME comments that should be addressed");
            checkPattern(issues, file.getFilename(), patch, "password|secret|api_key|apikey|token",
                    "Security", "Potential hardcoded credentials detected");
            checkPattern(issues, file.getFilename(), patch, "catch\\s*\\(Exception|catch\\s*\\(\\s*\\):",
                    "Error Handling", "Catching generic exceptions - consider more specific handling");

            // Language-specific patterns
            if ("Java".equals(language)) {
                checkPattern(issues, file.getFilename(), patch, "System\\.out\\.print",
                        "Code Quality", "Use a proper logging framework instead of System.out.println");
                checkPattern(issues, file.getFilename(), patch, "\\.equals\\(null\\)",
                        "Bug Risk", "Use == null instead of .equals(null)");
                checkPattern(issues, file.getFilename(), patch, "e\\.printStackTrace\\(\\)",
                        "Code Quality", "Use a logger instead of printStackTrace()");
            } else if ("Python".equals(language)) {
                checkPattern(issues, file.getFilename(), patch, "except:\\s*$|except Exception:",
                        "Error Handling", "Bare except clause - specify exception types");
                checkPattern(issues, file.getFilename(), patch, "import \\*",
                        "Code Quality", "Wildcard imports reduce readability");
                checkPattern(issues, file.getFilename(), patch, "eval\\(",
                        "Security", "Avoid using eval() - security risk");
            } else if ("JavaScript".equals(language)) {
                checkPattern(issues, file.getFilename(), patch, "console\\.log",
                        "Code Quality", "Remove console.log statements before production");
                checkPattern(issues, file.getFilename(), patch, "var ",
                        "Code Quality", "Use let/const instead of var");
                checkPattern(issues, file.getFilename(), patch, "==(?!=)",
                        "Bug Risk", "Use === instead of == for comparison");
            }
        }

        return issues;
    }

    private void checkPattern(List<Map<String, Object>> issues, String filename,
                               String content, String pattern, String category, String message) {
        if (content.toLowerCase().matches("(?s).*(" + pattern.toLowerCase() + ").*")) {
            Map<String, Object> issue = new LinkedHashMap<>();
            issue.put("file", filename);
            issue.put("category", category);
            issue.put("message", message);
            issue.put("severity", "MEDIUM");
            issues.add(issue);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getAnalysisSummary(List<AnalysisResult> results) {
        if (results.isEmpty()) return "No static analysis performed.";
        StringBuilder sb = new StringBuilder();
        for (AnalysisResult r : results) {
            sb.append(String.format("Tool: %s (%s) - %d issues found\n", r.getToolName(), r.getLanguage(), r.getIssueCount()));
            if (r.getIssues() != null && !r.getIssues().equals("[]")) {
                sb.append("Issues: ").append(r.getIssues(), 0, Math.min(500, r.getIssues().length())).append("\n");
            }
        }
        return sb.toString();
    }
}
