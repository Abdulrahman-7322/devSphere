package com.shutu.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shutu.interview.entity.Interview;
import com.shutu.interview.mapper.InterviewMapper;
import com.shutu.interview.service.InterviewService;
import org.springframework.stereotype.Service;

@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {
}
