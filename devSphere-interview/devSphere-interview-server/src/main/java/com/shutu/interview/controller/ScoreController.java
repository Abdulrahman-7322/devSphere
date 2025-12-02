package com.shutu.interview.controller;

import com.shutu.interview.entity.Score;
import com.shutu.interview.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoresService;

    @GetMapping
    public List<Score> list() {
        return scoresService.list();
    }

    @GetMapping("/{id}")
    public Score getById(@PathVariable Long id) {
        return scoresService.getById(id);
    }

    @PostMapping
    public boolean save(@RequestBody Score score) {
        return scoresService.save(score);
    }

    @PutMapping
    public boolean update(@RequestBody Score score) {
        return scoresService.updateById(score);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return scoresService.removeById(id);
    }
}
