package com.shutu.interview.service.impl;

import com.shutu.interview.entity.Interview;
import com.shutu.interview.entity.InterviewLog;
import com.shutu.interview.entity.Question;
import com.shutu.interview.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterviewFlowServiceImpl implements InterviewFlowService {

    private final InterviewStateService interviewStateService;
    private final QuestionGenerationService questionGenerationService;
    private final InterviewLogService interviewLogService;
    private final InterviewService interviewService;
    private final QuestionService questionService;

    // TODO: Fetch from Resume/Job Service
    private static final String MOCK_RESUME = "Java Developer with 5 years experience in Spring Boot, MySQL, and Microservices.";
    private static final String MOCK_JD = "Looking for a Senior Java Engineer proficient in high concurrency, distributed systems, and AI integration.";

    public InterviewFlowServiceImpl(InterviewStateService interviewStateService,
                                    QuestionGenerationService questionGenerationService,
                                    InterviewLogService interviewLogService,
                                    InterviewService interviewService,
                                    QuestionService questionService) {
        this.interviewStateService = interviewStateService;
        this.questionGenerationService = questionGenerationService;
        this.interviewLogService = interviewLogService;
        this.interviewService = interviewService;
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public Question startInterview(Long interviewId) {
        // 1. Update State
        interviewStateService.start(interviewId);

        // 2. Generate Questions (Pre-generate or Just-in-Time)
        // For simplicity, we generate the first batch here and save them
        List<Question> questions = questionGenerationService.generateQuestions(MOCK_RESUME, MOCK_JD, 5);
        
        // Save generated questions to DB if they are new (optional, or just use them for this session)
        // Here we assume we pick the first one
        if (questions.isEmpty()) {
            throw new RuntimeException("Failed to generate questions");
        }
        
        Question firstQuestion = questions.get(0);
        // Ensure question is saved so we have an ID
        if (firstQuestion.getId() == null) {
            questionService.save(firstQuestion);
        }

        // 3. Create Log Entry for Round 1
        createLog(interviewId, 1, firstQuestion.getContent());

        return firstQuestion;
    }

    @Override
    @Transactional
    public Question submitAnswer(Long interviewId, String answerText) {
        // 1. Find current active log
        InterviewLog currentLog = interviewLogService.lambdaQuery()
                .eq(InterviewLog::getInterviewId, interviewId)
                .orderByDesc(InterviewLog::getRound)
                .last("LIMIT 1")
                .one();

        if (currentLog == null) {
            throw new RuntimeException("No active question found for interview: " + interviewId);
        }

        // 2. Save Answer
        currentLog.setAnswerText(answerText);
        // TODO: Async Grading Trigger
        interviewLogService.updateById(currentLog);

        // 3. Determine Next Step (Follow-up or Next Question)
        Question currentQuestion = new Question();
        currentQuestion.setContent(currentLog.getQuestionText());
        // We might need more info about the question, but content is key for follow-up

        Question followUp = questionGenerationService.generateFollowUp(currentQuestion, answerText);
        
        if (followUp != null) {
            // Ask Follow-up
            createLog(interviewId, currentLog.getRound() + 1, followUp.getContent());
            return followUp;
        } else {
            // Next New Question
            // In a real app, we would retrieve the pre-generated list or generate a new one based on progress
            // Here we simply generate ONE new question for the next round
            List<Question> nextQuestions = questionGenerationService.generateQuestions(MOCK_RESUME, MOCK_JD, 1);
            if (nextQuestions.isEmpty()) {
                // End Interview if no more questions
                interviewStateService.complete(interviewId);
                return null;
            }
            Question nextQ = nextQuestions.get(0);
            if (nextQ.getId() == null) {
                questionService.save(nextQ);
            }
            createLog(interviewId, currentLog.getRound() + 1, nextQ.getContent());
            return nextQ;
        }
    }

    private void createLog(Long interviewId, int round, String questionText) {
        InterviewLog log = new InterviewLog();
        log.setInterviewId(interviewId);
        log.setRound(round);
        log.setQuestionText(questionText);
        interviewLogService.save(log);
    }
}
