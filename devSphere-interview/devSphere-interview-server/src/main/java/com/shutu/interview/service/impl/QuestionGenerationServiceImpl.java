package com.shutu.interview.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutu.interview.entity.Question;
import com.shutu.interview.service.AIService;
import com.shutu.interview.service.QuestionGenerationService;
import com.shutu.interview.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionGenerationServiceImpl implements QuestionGenerationService {

    private final AIService aiService;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    public QuestionGenerationServiceImpl(AIService aiService, QuestionService questionService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.questionService = questionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Question> generateQuestions(String resumeText, String jobDescription, int count) {
        // 1. Extract Keywords
        String keywords = extractKeywords(resumeText, jobDescription);

        // 2. Search Vector DB for relevant questions
        List<Question> similarQuestions = questionService.searchSimilar(keywords, 5);
        String existingQuestionsContext = similarQuestions.stream()
                .map(q -> "- " + q.getContent())
                .collect(Collectors.joining("\n"));

        // 3. Generate Questions using LLM
        String systemPrompt = "You are an expert technical interviewer. Your goal is to generate interview questions based on the candidate's resume and the job description.";
        String userPrompt = String.format("""
                Resume:
                %s
                
                Job Description:
                %s
                
                Existing Question Database (for reference, avoid exact duplicates but cover similar topics):
                %s
                
                Task: Generate %d technical interview questions.
                Format: Return a JSON array of objects with 'content', 'category', 'difficulty' (1-5), and 'standardAnswer'.
                Example:
                [
                    {"content": "Explain HashMap working principle", "category": "Java", "difficulty": 3, "standardAnswer": "HashMap uses hashing..."}
                ]
                """, resumeText, jobDescription, existingQuestionsContext, count);

        String jsonResponse = aiService.chat(systemPrompt, userPrompt);
        return parseQuestions(jsonResponse);
    }

    @Override
    public Question generateFollowUp(Question currentQuestion, String userAnswer) {
        String systemPrompt = "You are an expert technical interviewer. Decide if a follow-up question is needed based on the candidate's answer.";
        String userPrompt = String.format("""
                Current Question: %s
                Candidate Answer: %s
                
                Task: If the answer is incomplete, vague, or interesting enough to dig deeper, generate a follow-up question.
                If the answer is good and complete, return 'NO_FOLLOWUP'.
                
                Format: Return a JSON object with 'content', 'category', 'difficulty', 'standardAnswer' OR just the string 'NO_FOLLOWUP'.
                """, currentQuestion.getContent(), userAnswer);

        String response = aiService.chat(systemPrompt, userPrompt);
        if (response.contains("NO_FOLLOWUP")) {
            return null;
        }
        try {
            // Clean up markdown code blocks if present
            String json = cleanJson(response);
            return objectMapper.readValue(json, Question.class);
        } catch (Exception e) {
            // Fallback: treat response as content if parsing fails
            Question q = new Question();
            q.setContent(response);
            q.setCategory(currentQuestion.getCategory());
            q.setDifficulty(currentQuestion.getDifficulty());
            return q;
        }
    }

    private String extractKeywords(String resume, String jd) {
        String prompt = String.format("""
                Extract top 5 technical keywords or topics from the following text for vector search.
                Return comma-separated string.
                
                Text:
                %s
                %s
                """, resume, jd);
        return aiService.chat(prompt);
    }

    private List<Question> parseQuestions(String jsonResponse) {
        try {
            String json = cleanJson(jsonResponse);
            return objectMapper.readValue(json, new TypeReference<List<Question>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String cleanJson(String response) {
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
}
