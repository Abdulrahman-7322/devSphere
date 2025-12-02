package com.shutu.interview.controller;

import com.shutu.interview.entity.InterviewLog;
import com.shutu.interview.service.InterviewLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interview-logs")
public class InterviewLogController {

    @Autowired
    private InterviewLogService interviewLogsService;

    @GetMapping
    public List<InterviewLog> list() {
        return interviewLogsService.list();
    }

    @GetMapping("/{id}")
    public InterviewLog getById(@PathVariable Long id) {
        return interviewLogsService.getById(id);
    }

    @PostMapping
    public boolean save(@RequestBody InterviewLog log) {
        return interviewLogsService.save(log);
    }

    @PutMapping
    public boolean update(@RequestBody InterviewLog log) {
        return interviewLogsService.updateById(log);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return interviewLogsService.removeById(id);
    }
}
