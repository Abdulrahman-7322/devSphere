-- 开启 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 用户表 (Users) 由 shutu-auth 模块管理 (MySQL)
-- 我们在 dev_sphere_interviews 表中只存储 user_id

-- 面试记录表 (dev_sphere_interviews)
CREATE TABLE IF NOT EXISTS dev_sphere_interviews (
    id BIGINT PRIMARY KEY, -- 面试ID (雪花算法)
    user_id BIGINT NOT NULL, -- 用户ID (关联 shutu-auth)
    job_id BIGINT NOT NULL, -- 岗位ID
    start_time TIMESTAMP WITH TIME ZONE, -- 开始时间
    end_time TIMESTAMP WITH TIME ZONE, -- 结束时间
    status SMALLINT DEFAULT 0, -- 状态: 0:准备中, 1:进行中, 2:已完成, 3:已终止
    total_score DECIMAL(5,2), -- 总分
    summary TEXT, -- 面试总结
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 创建时间
);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_interviews_user_id ON dev_sphere_interviews(user_id);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_interviews_job_id ON dev_sphere_interviews(job_id);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_interviews_status ON dev_sphere_interviews(status);

-- 面试题目表 (dev_sphere_questions)
CREATE TABLE IF NOT EXISTS dev_sphere_questions (
    id BIGINT PRIMARY KEY, -- 题目ID (雪花算法)
    content TEXT NOT NULL, -- 题目内容
    embedding vector(1536), -- 题目向量 (OpenAI/DeepSeek 1536维)
    category VARCHAR(32), -- 分类 (如: Java, SystemDesign)
    difficulty SMALLINT, -- 难度 (1-5)
    standard_answer TEXT, -- 参考答案
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 创建时间
);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_questions_category ON dev_sphere_questions(category);

-- 面试问答日志表 (dev_sphere_interview_logs)
CREATE TABLE IF NOT EXISTS dev_sphere_interview_logs (
    id BIGINT PRIMARY KEY, -- 日志ID (雪花算法)
    interview_id BIGINT NOT NULL, -- 关联面试ID
    round INT NOT NULL, -- 轮次 (第几题)
    question_text TEXT, -- 当时的问题文本
    answer_audio_url VARCHAR(255), -- 回答录音地址 (OSS)
    answer_text TEXT, -- 回答转录文本 (ASR)
    score DECIMAL(5,2), -- 单题得分
    evaluation TEXT, -- 单题评价/建议
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 创建时间
);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_interview_logs_interview_id ON dev_sphere_interview_logs(interview_id);

-- 评分结果表 (dev_sphere_scores)
CREATE TABLE IF NOT EXISTS dev_sphere_scores (
    id BIGINT PRIMARY KEY, -- 评分ID (雪花算法)
    interview_id BIGINT NOT NULL, -- 关联面试ID
    dimension VARCHAR(32) NOT NULL, -- 评分维度 (如: Knowledge, Logic, Communication)
    score DECIMAL(5,2), -- 分数
    comment TEXT, -- 维度评语
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 创建时间
);
CREATE INDEX IF NOT EXISTS idx_dev_sphere_scores_interview_id ON dev_sphere_scores(interview_id);

-- 添加表注释 (PostgreSQL 特有语法)
COMMENT ON TABLE dev_sphere_interviews IS '面试记录表';
COMMENT ON COLUMN dev_sphere_interviews.id IS '面试ID';
COMMENT ON COLUMN dev_sphere_interviews.user_id IS '用户ID';
COMMENT ON COLUMN dev_sphere_interviews.job_id IS '岗位ID';
COMMENT ON COLUMN dev_sphere_interviews.start_time IS '开始时间';
COMMENT ON COLUMN dev_sphere_interviews.end_time IS '结束时间';
COMMENT ON COLUMN dev_sphere_interviews.status IS '状态: 0:准备中, 1:进行中, 2:已完成, 3:已终止';
COMMENT ON COLUMN dev_sphere_interviews.total_score IS '总分';
COMMENT ON COLUMN dev_sphere_interviews.summary IS '面试总结';

COMMENT ON TABLE dev_sphere_questions IS '面试题目表';
COMMENT ON COLUMN dev_sphere_questions.content IS '题目内容';
COMMENT ON COLUMN dev_sphere_questions.embedding IS '题目向量';
COMMENT ON COLUMN dev_sphere_questions.category IS '分类';
COMMENT ON COLUMN dev_sphere_questions.difficulty IS '难度';
COMMENT ON COLUMN dev_sphere_questions.standard_answer IS '参考答案';

COMMENT ON TABLE dev_sphere_interview_logs IS '面试问答日志表';
COMMENT ON COLUMN dev_sphere_interview_logs.interview_id IS '关联面试ID';
COMMENT ON COLUMN dev_sphere_interview_logs.round IS '轮次';
COMMENT ON COLUMN dev_sphere_interview_logs.question_text IS '问题文本';
COMMENT ON COLUMN dev_sphere_interview_logs.answer_audio_url IS '回答录音地址';
COMMENT ON COLUMN dev_sphere_interview_logs.answer_text IS '回答转录文本';
COMMENT ON COLUMN dev_sphere_interview_logs.score IS '单题得分';
COMMENT ON COLUMN dev_sphere_interview_logs.evaluation IS '单题评价';

COMMENT ON TABLE dev_sphere_scores IS '评分结果表';
COMMENT ON COLUMN dev_sphere_scores.interview_id IS '关联面试ID';
COMMENT ON COLUMN dev_sphere_scores.dimension IS '评分维度';
COMMENT ON COLUMN dev_sphere_scores.score IS '分数';
COMMENT ON COLUMN dev_sphere_scores.comment IS '维度评语';
