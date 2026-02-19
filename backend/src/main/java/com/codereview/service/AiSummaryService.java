package com.codereview.service;

import com.codereview.client.GroqClient;
import com.codereview.dto.ApiDtos;
import com.codereview.model.AiSummary;
import com.codereview.model.AnalysisResult;
import com.codereview.model.Repository;
import com.codereview.repository.AiSummaryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSummaryService {

    private final AiSummaryRepository aiSummaryRepository;
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public AiSummary generateAndSaveSummary(Repository repository, String commitSha,
                                             String commitMessage, String diff,
                                             List<AnalysisResult> analysisResults) {
        // Check cache
        Optional<AiSummary> existing = aiSummaryRepository.findByCommitSha(commitSha);
        if (existing.isPresent()) {
            log.debug("AI summary already exists for commit {}", commitSha);
            return existing.get();
        }

        String staticAnalysisSummary = buildAnalysisSummary(analysisResults);
        String aiResponse = groqClient.generateCodeReview(commitMessage, diff, staticAnalysisSummary);
        
        return parseAndSaveSummary(repository, commitSha, aiResponse);
    }

    private AiSummary parseAndSaveSummary(Repository repository, String commitSha, String aiResponse) {
        String summary = "Unable to parse AI response";
        int riskScore = 5;
        String improvements = "";
        String securityConcerns = "";

        try {
            // Clean up response - remove markdown code blocks if present
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            JsonNode node = objectMapper.readTree(cleaned);
            summary = node.path("summary").asText("Code changes analyzed");
            riskScore = node.path("risk_score").asInt(5);
            riskScore = Math.max(1, Math.min(10, riskScore)); // clamp 1-10

            JsonNode improvNode = node.path("improvements");
            if (improvNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode imp : improvNode) sb.append("• ").append(imp.asText()).append("\n");
                improvements = sb.toString();
            } else {
                improvements = improvNode.asText("");
            }

            JsonNode secNode = node.path("security_concerns");
            if (secNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode s : secNode) sb.append("• ").append(s.asText()).append("\n");
                securityConcerns = sb.toString();
            } else {
                securityConcerns = secNode.asText("");
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, using raw text: {}", e.getMessage());
            summary = aiResponse.substring(0, Math.min(aiResponse.length(), 500));
        }

        AiSummary aiSummary = AiSummary.builder()
                .repository(repository)
                .commitSha(commitSha)
                .summary(summary)
                .riskScore(riskScore)
                .riskLevel(getRiskLevel(riskScore))
                .improvements(improvements)
                .securityConcerns(securityConcerns)
                .build();

        return aiSummaryRepository.save(aiSummary);
    }

    private String buildAnalysisSummary(List<AnalysisResult> results) {
        if (results == null || results.isEmpty()) return "No static analysis results";
        StringBuilder sb = new StringBuilder();
        for (AnalysisResult r : results) {
            sb.append(String.format("Tool: %s, Language: %s, Issues: %d\n",
                    r.getToolName(), r.getLanguage(), r.getIssueCount()));
        }
        return sb.toString();
    }

    public static String getRiskLevel(int score) {
        if (score >= 7) return "HIGH";
        if (score >= 4) return "MEDIUM";
        return "LOW";
    }

    public Optional<AiSummary> findByCommitSha(String sha) {
        return aiSummaryRepository.findByCommitSha(sha);
    }

    public ApiDtos.AiSummaryDto toDto(AiSummary summary) {
        return ApiDtos.AiSummaryDto.builder()
                .id(summary.getId())
                .commitSha(summary.getCommitSha())
                .summary(summary.getSummary())
                .riskScore(summary.getRiskScore())
                .riskLevel(summary.getRiskLevel())
                .improvements(summary.getImprovements())
                .securityConcerns(summary.getSecurityConcerns())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
