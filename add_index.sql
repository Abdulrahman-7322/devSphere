-- 优化聊天记录查询性能
-- 添加 (room_id, id) 联合索引
-- room_id 用于快速定位会话
-- id 用于范围查询 (id < cursor) 和排序 (ORDER BY id DESC)

ALTER TABLE `dev_sphere_message` 
ADD INDEX `idx_room_id_id` (`room_id`, `id`);
