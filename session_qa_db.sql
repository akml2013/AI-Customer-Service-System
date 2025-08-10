/*
 Navicat Premium Data Transfer

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : localhost:3306
 Source Schema         : session_qa_db

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 21/07/2025 19:59:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for qa_pair
-- ----------------------------
DROP TABLE IF EXISTS `qa_pair`;
CREATE TABLE `qa_pair`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '问答ID',
  `session_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '会话ID',
  `question` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户问题',
  `answer` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'AI回复',
  `ask_time` datetime(3) NOT NULL COMMENT '提问时间',
  `answer_time` datetime(3) NULL DEFAULT NULL COMMENT '回复时间',
  `response_duration` int(11) NULL DEFAULT NULL COMMENT '响应耗时(ms)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session`(`session_id`) USING BTREE,
  FULLTEXT INDEX `idx_question`(`question`),
  CONSTRAINT `qa_pair_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 107683 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for session
-- ----------------------------
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '会话ID(UUID)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '会话标题（首条问题）',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_activity` datetime(0) NULL DEFAULT NULL COMMENT '最后活动时间',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态(0关闭,1活跃)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE,
  INDEX `idx_last_activity`(`last_activity`) USING BTREE,
  CONSTRAINT `session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for session_access_log
-- ----------------------------
DROP TABLE IF EXISTS `session_access_log`;
CREATE TABLE `session_access_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `access_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `operation` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作类型(create,read,delete)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_user`(`session_id`, `user_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `session_access_log_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `session_access_log_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 45383 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `branch_id` bigint(20) NOT NULL COMMENT '分支事务ID',
  `xid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '全局事务唯一标识',
  `context` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '上下文',
  `rollback_info` longblob NOT NULL COMMENT '回滚信息',
  `log_status` int(11) NOT NULL COMMENT '状态，0正常，1全局已完成（防悬挂）',
  `log_created` datetime(0) NOT NULL COMMENT '创建时间',
  `log_modified` datetime(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid`, `branch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'AT模式回滚日志表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
