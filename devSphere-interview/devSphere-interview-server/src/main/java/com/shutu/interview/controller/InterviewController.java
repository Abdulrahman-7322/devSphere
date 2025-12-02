package com.shutu.interview.controller;

import com.shutu.interview.entity.Interview;
import com.shutu.interview.entity.Question;
import com.shutu.interview.service.InterviewFlowService;
import com.shutu.interview.service.InterviewService;
import com.shutu.interview.service.InterviewStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewsService;

    @Autowired
    private InterviewStateService interviewStateService;

    @Autowired
    private InterviewFlowService interviewFlowService;

    @GetMapping
    public List<Interview> list() {
        return interviewsService.list();
    }

    @GetMapping("/{id}")
    public Interview getById(@PathVariable Long id) {
        return interviewsService.getById(id);
    }

    @PostMapping
    public Interview save(@RequestBody Interview interview) {
        interviewsService.save(interview);
        return interview;
    }

    @PutMapping
    public boolean update(@RequestBody Interview interview) {
        return interviewsService.updateById(interview);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return interviewsService.removeById(id);
    }

    // --- Business Logic Endpoints ---

    @PostMapping("/{id}/start")
    public Question start(@PathVariable Long id) {
        return interviewFlowService.startInterview(id);
    }

    @PostMapping("/{id}/submit")
    public Question submit(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String answer = payload.get("answer");
        return interviewFlowService.submitAnswer(id, answer);
    }

    @PostMapping("/{id}/complete")
    public Interview complete(@PathVariable Long id) {
        return interviewStateService.complete(id);
    }

    @PostMapping("/{id}/abort")
    public Interview abort(@PathVariable Long id) {
        return interviewStateService.abort(id);
    }
}
