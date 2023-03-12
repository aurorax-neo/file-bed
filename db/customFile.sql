SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `customFile`;
CREATE TABLE `customFile`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `fileName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `fileSize` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
  `filePath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `segmentIndex` int(11) NULL DEFAULT NULL COMMENT '分片索引',
  `segmentSize` bigint(20) NULL DEFAULT NULL COMMENT '分片大小',
  `segmentTotal` int(11) NULL DEFAULT NULL COMMENT '分片总数',
  `isMerge` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否合并',
  `isST` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否秒传',
  `fileMD5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MD5',
  `fileKey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'key',
  `uploadDate` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上传日期',
  `isDelete` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fileKey_index`(`fileKey`) USING BTREE COMMENT 'fileKey索引',
  INDEX `fileMD5_index`(`fileMD5`) USING BTREE COMMENT 'fileMD5索引'
) ENGINE = InnoDB AUTO_INCREMENT = 145 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
