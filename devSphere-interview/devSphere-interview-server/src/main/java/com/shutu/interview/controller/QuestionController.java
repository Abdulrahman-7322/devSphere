package com.shutu.interview.controller;

import com.shutu.interview.entity.Question;
import com.shutu.interview.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionsService;

    @GetMapping
    public List<Question> list() {
        return questionsService.list();
    }

    @GetMapping("/{id}")
    public Question getById(@PathVariable Long id) {
        return questionsService.getById(id);
    }

    @GetMapping("/search")
    public List<Question> search(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        return questionsService.searchSimilar(query, limit);
    }

    @PostMapping
    public boolean save(@RequestBody Question question) {
        return questionsService.save(question);
    }

    @PutMapping
    public boolean update(@RequestBody Question question) {
        return questionsService.updateById(question);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return questionsService.removeById(id);
    }
}
