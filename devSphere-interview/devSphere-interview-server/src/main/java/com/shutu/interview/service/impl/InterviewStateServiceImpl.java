package com.shutu.interview.service.impl;

import com.shutu.interview.entity.Interview;
import com.shutu.interview.service.InterviewService;
import com.shutu.interview.service.InterviewStateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InterviewStateServiceImpl implements InterviewStateService {

    private final InterviewService interviewService;

    // Status Constants
    public static final int STATUS_READY = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_ABORTED = 3;

    public InterviewStateServiceImpl(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @Override
    @Transactional
    public Interview start(Long interviewId) {
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("Interview not found: " + interviewId);
        }
        if (interview.getStatus() != STATUS_READY) {
            throw new RuntimeException("Interview cannot be started. Current status: " + interview.getStatus());
        }

        interview.setStatus(STATUS_IN_PROGRESS);
        interview.setStartTime(LocalDateTime.now());
        interviewService.updateById(interview);
        return interview;
    }

    @Override
    @Transactional
    public Interview complete(Long interviewId) {
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("Interview not found: " + interviewId);
        }
        if (interview.getStatus() != STATUS_IN_PROGRESS) {
            throw new RuntimeException("Interview cannot be completed. Current status: " + interview.getStatus());
        }

        interview.setStatus(STATUS_COMPLETED);
        interview.setEndTime(LocalDateTime.now());
        interviewService.updateById(interview);
        return interview;
    }

    @Override
    @Transactional
    public Interview abort(Long interviewId) {
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("Interview not found: " + interviewId);
        }
        // Can abort from Ready or In Progress
        if (interview.getStatus() == STATUS_COMPLETED || interview.getStatus() == STATUS_ABORTED) {
            throw new RuntimeException("Interview cannot be aborted. Current status: " + interview.getStatus());
        }

        interview.setStatus(STATUS_ABORTED);
        interview.setEndTime(LocalDateTime.now());
        interviewService.updateById(interview);
        return interview;
    }

    @Override
    public boolean isInProgress(Integer status) {
        return status != null && status == STATUS_IN_PROGRESS;
    }
}
