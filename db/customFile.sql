SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for CUSTOMFILE
-- ----------------------------
DROP TABLE IF EXISTS `CUSTOMFILE`;
CREATE TABLE `CUSTOMFILE`
(
    `ID`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
    `FILENAME`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
    `FILESIZE`     bigint(20)                                                    NULL DEFAULT NULL COMMENT '文件大小',
    `FILEPATH`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
    `SEGMENTINDEX` int(11)                                                       NULL DEFAULT NULL COMMENT '分片索引',
    `SEGMENTSIZE`  bigint(20)                                                    NULL DEFAULT NULL COMMENT '分片大小',
    `SEGMENTTOTAL` int(11)                                                       NULL DEFAULT NULL COMMENT '分片总数',
    `ISMERGE`      tinyint(1) UNSIGNED                                           NULL DEFAULT 0 COMMENT '是否合并',
    `ISST`         tinyint(1) UNSIGNED                                           NULL DEFAULT 0 COMMENT '是否秒传',
    `FILEMD5`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MD5',
    `FILEKEY`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'KEY',
    `UPLOADDATE`   datetime                                                      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上传日期',
    `ISDELETE`     tinyint(1) UNSIGNED                                           NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`ID`) USING BTREE,
    INDEX `FILEKEY_INDEX` (`FILEKEY`) USING BTREE COMMENT 'FILEKEY索引',
    INDEX `FILEMD5_INDEX` (`FILEMD5`) USING BTREE COMMENT 'FILEMD5索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
