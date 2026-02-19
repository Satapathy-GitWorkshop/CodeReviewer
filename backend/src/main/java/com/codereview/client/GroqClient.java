package com.codereview.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class GroqClient {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama3-70b-8192";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GroqClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${groq.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public String generateCodeReview(String commitMessage, String diff, String staticAnalysisResults) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Groq API key not configured, returning mock response");
            return generateMockReview(commitMessage);
        }

        String prompt = buildPrompt(commitMessage, diff, staticAnalysisResults);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.3);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "You are an expert code reviewer. Analyze code changes and provide structured feedback in JSON format."
            ));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(GROQ_API_URL, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error calling Groq API: {}", e.getMessage());
            return generateMockReview(commitMessage);
        }
    }

    private String buildPrompt(String commitMessage, String diff, String staticAnalysis) {
        return String.format("""
            Analyze this code commit and provide a review in the following JSON format:
            {
              "summary": "Brief description of changes",
              "risk_score": 1-10,
              "risk_explanation": "Why this risk score",
              "improvements": ["improvement 1", "improvement 2"],
              "security_concerns": ["concern 1"] or []
            }
            
            Commit Message: %s
            
            Static Analysis Results:
            %s
            
            Code Diff (first 3000 chars):
            %s
            
            Respond ONLY with valid JSON, no extra text.
            """,
            commitMessage,
            staticAnalysis.substring(0, Math.min(staticAnalysis.length(), 1000)),
            diff.substring(0, Math.min(diff.length(), 3000)));
    }

    private String generateMockReview(String commitMessage) {
        return String.format("""
            {
              "summary": "Code changes analyzed for commit: %s",
              "risk_score": 3,
              "risk_explanation": "Minor changes with low risk - API key not configured for real analysis",
              "improvements": ["Add unit tests", "Document public methods", "Consider edge cases"],
              "security_concerns": []
            }
            """, commitMessage.replace("\"", "'"));
    }
}
