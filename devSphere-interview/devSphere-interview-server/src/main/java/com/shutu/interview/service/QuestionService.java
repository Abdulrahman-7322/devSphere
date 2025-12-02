package com.shutu.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shutu.interview.entity.Question;
import java.util.List;

public interface QuestionService extends IService<Question> {
    List<Question> searchSimilar(String query, int limit);
}
