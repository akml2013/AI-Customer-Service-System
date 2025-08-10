/*
 Navicat Premium Data Transfer

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : localhost:3306
 Source Schema         : session_knowledge_db

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 21/07/2025 19:58:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for knowledge
-- ----------------------------
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `question` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '问题',
  `answer` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '解答',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_activity` datetime(0) NULL DEFAULT NULL COMMENT '最后活动时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

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
