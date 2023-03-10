/*
 Navicat Premium Data Transfer

 Source Server         : Mariadb.root@127.0.0.1
 Source Server Type    : MariaDB
 Source Server Version : 101002 (10.10.2-MariaDB)
 Source Host           : localhost:3306
 Source Schema         : drawing_bed

 Target Server Type    : MariaDB
 Target Server Version : 101002 (10.10.2-MariaDB)
 File Encoding         : 65001

 Date: 08/03/2023 20:15:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for segmentfile
-- ----------------------------
DROP TABLE IF EXISTS `segmentfile`;
CREATE TABLE `segmentfile`
(
    `id`           bigint(20) UNSIGNED                                           NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
    `suffix`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '文件后缀',
    `size`         bigint(20)                                                    NULL DEFAULT NULL COMMENT '文件大小',
    `path`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
    `url`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件链接',
    `segmentIndex` int(11)                                                       NULL DEFAULT NULL COMMENT '分片索引',
    `segmentSize`  bigint(20)                                                    NULL DEFAULT NULL COMMENT '分片大小',
    `segmentTotal` int(11)                                                       NULL DEFAULT NULL COMMENT '分片总数',
    `customKey`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'key',
    `uploadDate`   datetime                                                      NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '上传日期',
    `isDelete`     tinyint(1) UNSIGNED                                           NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `md5Key_index` (`customKey`) USING BTREE COMMENT 'md5Key索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 22
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
