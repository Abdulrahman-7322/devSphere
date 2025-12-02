package com.shutu.interview.service;

import com.shutu.interview.entity.Interview;

public interface InterviewStateService {

    /**
     * Start the interview
     * @param interviewId Interview ID
     * @return Updated Interview entity
     */
    Interview start(Long interviewId);

    /**
     * Complete the interview
     * @param interviewId Interview ID
     * @return Updated Interview entity
     */
    Interview complete(Long interviewId);

    /**
     * Abort the interview
     * @param interviewId Interview ID
     * @return Updated Interview entity
     */
    Interview abort(Long interviewId);

    /**
     * Check if interview is in progress
     * @param status Interview status
     * @return true if in progress
     */
    boolean isInProgress(Integer status);
}
