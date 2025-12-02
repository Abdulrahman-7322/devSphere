package com.shutu.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.interview.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM dev_sphere_questions ORDER BY embedding <-> #{embedding, typeHandler=com.shutu.interview.handler.PGVectorTypeHandler} LIMIT #{limit}")
    List<Question> searchSimilar(@Param("embedding") List<Double> embedding, @Param("limit") int limit);
}
