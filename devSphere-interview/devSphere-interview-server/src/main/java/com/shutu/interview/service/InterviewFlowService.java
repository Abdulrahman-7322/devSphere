package com.shutu.interview.service;

import com.shutu.interview.entity.Interview;
import com.shutu.interview.entity.Question;

public interface InterviewFlowService {

    /**
     * Start a new interview session
     * @param interviewId Interview ID
     * @return The first question
     */
    Question startInterview(Long interviewId);

    /**
     * Submit an answer and get the next question
     * @param interviewId Interview ID
     * @param answerText Candidate's answer
     * @return The next Question (or null if interview is finished)
     */
    Question submitAnswer(Long interviewId, String answerText);
}
