package com.shutu.interview.service;

import com.shutu.interview.entity.Question;

import java.util.List;

public interface QuestionGenerationService {

    /**
     * Generate a list of questions based on resume and job description
     * @param resumeText Resume content
     * @param jobDescription Job description
     * @param count Number of questions to generate
     * @return List of questions
     */
    List<Question> generateQuestions(String resumeText, String jobDescription, int count);

    /**
     * Generate a follow-up question based on the previous answer
     * @param currentQuestion The question just asked
     * @param userAnswer The user's answer
     * @return A new Question object for the follow-up, or null if no follow-up needed
     */
    Question generateFollowUp(Question currentQuestion, String userAnswer);
}
