/*
 Navicat Premium Data Transfer

 Source Server         : Mysql
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : localhost:3306
 Source Schema         : session_user_db

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 21/07/2025 19:59:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '加密密码',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态(0禁用,1启用)',
  `role` tinyint(4) NULL DEFAULT 1 COMMENT '权限(0管理员,1用户)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'abc', '$2a$10$2sR/X42ReGt.MD.sONyrjeIpZZxibo9qX8j./ey0jHjhJbLIIsjy2', '1183418080@qq.com', '15564929795', '2025-07-18 21:51:55', '2025-07-21 18:56:26', 1, 2);
INSERT INTO `user` VALUES (14, 'bbb', '$2a$10$/nA1LWGBCFPmKN/KPWub/uHgZT7iZVAs1Nh80.N0qyauR7cCThYQC', '118341800@qq.com', '15564929795', '2025-07-19 10:42:48', '2025-07-21 14:52:53', 1, 1);
INSERT INTO `user` VALUES (15, 'ccc', '$2a$10$lwDHSnlMoDaWZAmjvW0aSOuAERa1hDhiumNZSCLGAaZ9s7BscOmQ6', '118341808@qq.com', '15564929795', '2025-07-19 10:50:34', '2025-07-20 20:38:40', 1, 1);
INSERT INTO `user` VALUES (16, 'akml', '$2a$10$EiK1UpHjgQaO48itaR.CAeVrmqIwhR9o7LjaiZ9fgWjNzxm4a/YuC', '6233111051@qq.com', '15564929795', '2025-07-21 15:06:38', '2025-07-21 17:42:20', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
