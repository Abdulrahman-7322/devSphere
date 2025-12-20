/*
 Navicat Premium Data Transfer

 Source Server         : spark
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42-0ubuntu0.24.04.1)
 Source Host           : 10.104.0.111:3306
 Source Schema         : dev_sphere

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42-0ubuntu0.24.04.1)
 File Encoding         : 65001

 Date: 25/11/2025 19:35:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dev_sphere_message
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_message`;
CREATE TABLE `dev_sphere_message`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `server_msg_id` bigint NULL DEFAULT NULL COMMENT '唯一id，客户端可见',
  `temp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端临时ID，用于幂等去重',
  `room_id` bigint NOT NULL COMMENT '会话表id',
  `from_uid` bigint NOT NULL COMMENT '消息发送者uid',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
  `reply_msg_id` bigint NULL DEFAULT NULL COMMENT '回复的消息内容',
  `status` int NOT NULL DEFAULT 0 COMMENT '消息状态 0正常 1删除',
  `gap_count` int NULL DEFAULT NULL COMMENT '与回复的消息间隔多少条',
  `type` int NULL DEFAULT 1 COMMENT '消息类型 1正常文本 2.撤回消息 3.图片 4.语音 5.视频 6.文件',
  `extra` json NULL COMMENT '扩展信息',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_server_msg_id`(`server_msg_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_from_uid`(`from_uid` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  INDEX `idx_room_id_id`(`room_id` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 637 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_message_error_log
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_message_error_log`;
CREATE TABLE `dev_sphere_message_error_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `temp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端临时ID',
  `from_uid` bigint NOT NULL COMMENT '发送者ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `type` int NULL DEFAULT NULL COMMENT '消息类型',
  `original_payload` json NULL COMMENT '原始Redis消息体',
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误原因',
  `status` int NULL DEFAULT 0 COMMENT '处理状态 0-待处理 1-已修复 2-忽略',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_temp_id`(`temp_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'IM消息死信日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dev_sphere_message_archive
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_message_archive`;
CREATE TABLE `dev_sphere_message_archive`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `server_msg_id` bigint NULL DEFAULT NULL COMMENT '唯一id',
  `temp_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端临时ID',
  `room_id` bigint NOT NULL COMMENT '会话表id',
  `from_uid` bigint NOT NULL COMMENT '消息发送者uid',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
  `reply_msg_id` bigint NULL DEFAULT NULL COMMENT '回复的消息内容',
  `status` int NOT NULL DEFAULT 0 COMMENT '消息状态 0正常 1删除',
  `gap_count` int NULL DEFAULT NULL COMMENT '与回复的消息间隔多少条',
  `type` int NULL DEFAULT 1 COMMENT '消息类型',
  `extra` json NULL COMMENT '扩展信息',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_server_msg_id`(`server_msg_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_room_id_create_time`(`room_id` ASC, `create_time` DESC) USING BTREE COMMENT '归档表核心查询索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息归档表(冷数据)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_notice_message
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_notice_message`;
CREATE TABLE `dev_sphere_notice_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `to_user_id` bigint NOT NULL COMMENT '接收用户id',
  `notice_type` int NULL DEFAULT NULL COMMENT '消息通知类型 1 系统通知 2 群聊通知 3 好友通知',
  `notice_content` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '消息通知内容',
  `read_target` tinyint NOT NULL DEFAULT 0 COMMENT '消息通知标识 0 未读 1 已读',
  `process_result` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '处理结果',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_room
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_room`;
CREATE TABLE `dev_sphere_room`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` int NOT NULL COMMENT '房间类型 1群聊 2私聊',
  `hot_flag` int NULL DEFAULT 0 COMMENT '是否全员展示 0否 1是',
  `active_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '房间最后消息的更新时间',
  `last_msg_id` bigint NULL DEFAULT NULL COMMENT '会话中的最后一条消息id',
  `ext_json` json NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30064 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_room_friend
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_room_friend`;
CREATE TABLE `dev_sphere_room_friend`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `room_id` bigint NOT NULL COMMENT '房间id',
  `uid1` bigint NOT NULL COMMENT 'uid1（更小的uid）',
  `uid2` bigint NOT NULL COMMENT 'uid2（更大的uid）',
  `room_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间key由两个uid拼接，先做排序uid1_uid2',
  `status` int NOT NULL DEFAULT 0 COMMENT '房间状态 0正常 1禁用(删好友了禁用)',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `roomKey`(`room_key` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户私聊表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_room_group
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_room_group`;
CREATE TABLE `dev_sphere_room_group`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `owner_id` bigint NOT NULL COMMENT '群主id',
  `room_id` bigint NOT NULL COMMENT '房间id',
  `name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群名称',
  `avatar` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群头像',
  `ext_json` json NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
  `delete_status` int NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群聊房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_user_friend_relate
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_user_friend_relate`;
CREATE TABLE `dev_sphere_user_friend_relate`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `relate_id` bigint NOT NULL COMMENT '关系 id',
  `relate_type` int NULL DEFAULT NULL COMMENT '关系类型 1群聊 2好友',
  `remark` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '好友备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_friend_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dev_sphere_user_room_relate
-- ----------------------------
DROP TABLE IF EXISTS `dev_sphere_user_room_relate`;
CREATE TABLE `dev_sphere_user_room_relate`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `room_id` bigint NOT NULL COMMENT '房间 id',
  `latest_read_msg_id` bigint NULL DEFAULT NULL COMMENT '最新已读 id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `min_msg_id` bigint NULL DEFAULT 0 COMMENT '消息起始可见ID(用于逻辑删除历史)',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除/隐藏会话 (0: 正常显示, 1: 删除/隐藏)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_room_id`(`user_id` ASC, `room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for moments_comment
-- ----------------------------
DROP TABLE IF EXISTS `moments_comment`;
CREATE TABLE `moments_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `reply_to_comment_id` bigint NULL DEFAULT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for moments_like
-- ----------------------------
DROP TABLE IF EXISTS `moments_like`;
CREATE TABLE `moments_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for moments_post
-- ----------------------------
DROP TABLE IF EXISTS `moments_post`;
CREATE TABLE `moments_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `like_count` int NULL DEFAULT 0,
  `comment_count` int NULL DEFAULT 0,
  `visibility` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1993282134931951619 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL COMMENT 'id',
  `pid` bigint NULL DEFAULT NULL COMMENT '上级ID',
  `pids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所有上级ID，用逗号分开',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '负责人ID',
  `sort` int UNSIGNED NULL DEFAULT NULL COMMENT '排序',
  `del_flag` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '删除标识  0：未删除    1：删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_language
-- ----------------------------
DROP TABLE IF EXISTS `sys_language`;
CREATE TABLE `sys_language`  (
  `table_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '表名',
  `table_id` bigint NOT NULL COMMENT '表主键',
  `field_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名',
  `field_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段值',
  `language` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言',
  PRIMARY KEY (`table_name`, `table_id`, `field_name`, `language`) USING BTREE,
  INDEX `idx_table_id`(`table_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '国际化' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL COMMENT 'id',
  `pid` bigint NULL DEFAULT NULL COMMENT '上级ID，一级菜单为0',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单URL',
  `menu_type` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '类型   0：菜单   1：按钮',
  `open_style` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '打开方式   0：内部   1：外部',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `permissions` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识，如：sys:menu:save',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `del_flag` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '删除标识  0：未删除    1：删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pid`(`pid` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `id` bigint NOT NULL COMMENT 'id',
  `url` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'URL地址',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '文件上传' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_params
-- ----------------------------
DROP TABLE IF EXISTS `sys_params`;
CREATE TABLE `sys_params`  (
  `id` bigint NOT NULL COMMENT 'id',
  `param_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数编码',
  `param_value` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数值',
  `param_type` tinyint UNSIGNED NULL DEFAULT 1 COMMENT '类型   0：系统参数   1：非系统参数',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '删除标识  0：未删除    1：删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_param_code`(`param_code` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参数管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint NOT NULL COMMENT 'id',
  `post_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位编码',
  `post_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位名称',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态  0：停用   1：正常',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '删除标识  0：未删除    1：删除',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_data_scope
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_data_scope`;
CREATE TABLE `sys_role_data_scope`  (
  `id` bigint NOT NULL COMMENT 'id',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色数据权限' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL COMMENT 'id',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '菜单ID',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_user`;
CREATE TABLE `sys_role_user`  (
  `id` bigint NOT NULL COMMENT 'id',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色用户关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT 'id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `unique_id` bigint NULL DEFAULT NULL COMMENT 'unionId',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台',
  `open_id` bigint NULL DEFAULT NULL COMMENT 'openId',
  `head_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'real_name',
  `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'nick_name',
  `gender` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '性别   0：男   1：女    2：保密',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `super_admin` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '超级管理员   0：否   1：是',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态  0：停用   1：正常',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '删除标识  0：未删除    1：删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `id` bigint NOT NULL COMMENT 'id',
  `post_id` bigint NULL DEFAULT NULL COMMENT '岗位ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户岗位关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_token`;
CREATE TABLE `sys_user_token`  (
  `id` bigint NOT NULL COMMENT 'id',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `access_token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'accessToken',
  `access_token_expire` datetime NULL DEFAULT NULL COMMENT 'accessToken 过期时间',
  `refresh_token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'refreshToken',
  `refresh_token_expire` datetime NULL DEFAULT NULL COMMENT 'refreshToken 过期时间',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户Token' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `user_id` bigint NOT NULL,
  `display_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bio` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` tinyint NULL DEFAULT NULL,
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
