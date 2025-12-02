package com.shutu.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shutu.interview.entity.InterviewLog;
import com.shutu.interview.mapper.InterviewLogMapper;
import com.shutu.interview.service.InterviewLogService;
import org.springframework.stereotype.Service;

@Service
public class InterviewLogServiceImpl extends ServiceImpl<InterviewLogMapper, InterviewLog> implements InterviewLogService {
}
