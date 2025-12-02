package com.shutu.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shutu.interview.entity.Question;
import com.shutu.interview.mapper.QuestionMapper;
import com.shutu.interview.service.QuestionService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final EmbeddingModel embeddingModel;

    public QuestionServiceImpl(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public boolean save(Question entity) {
        if (entity.getContent() != null && (entity.getEmbedding() == null || entity.getEmbedding().isEmpty())) {
            entity.setEmbedding(embeddingModel.embed(entity.getContent()));
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Question entity) {
        if (entity.getContent() != null) {
            // Re-generate embedding if content changes
            entity.setEmbedding(embeddingModel.embed(entity.getContent()));
        }
        return super.updateById(entity);
    }

    public List<Question> searchSimilar(String query, int limit) {
        List<Double> embedding = embeddingModel.embed(query);
        return baseMapper.searchSimilar(embedding, limit);
    }
}
