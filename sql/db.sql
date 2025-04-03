
DROP DATABASE irrigation;

CREATE DATABASE irrigation;

use irrigation;


CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                        `userName` varchar(255) NOT NULL COMMENT '用户名称',
                        `userAccount` varchar(255) NOT NULL COMMENT '用户账号',
                        `userPassword` varchar(1024) NOT NULL COMMENT '用户密码',
                        `userPhone` char(11) DEFAULT NULL COMMENT '用户手机号',
                        `userRole` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '用户权限: 0:普通用户/1:管理员',
                        `userPicture` varchar(1024) DEFAULT NULL COMMENT '用户头像',
                        `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                        `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `device` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                          `deviceId` varchar(255) NOT NULL COMMENT '设备编号',
                          `deviceType` tinyint NOT NULL COMMENT '设备类型（0：阀门/1：水闸/2：施肥机）',
                          `deviceManagerNumber` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备管理对应的分组/灌溉区域编号',
                          `deviceStatus` tinyint DEFAULT NULL COMMENT '设备状态（0表示关；1表示开）',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `field` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                         `fieldId` varchar(255) NOT NULL COMMENT '地块编号',
                         `fieldName` varchar(255) NOT NULL COMMENT '地块名称',
                         `fieldParent` bigint DEFAULT NULL COMMENT '地块id（用于区分基本灌溉单元）',
                         `fieldUnitId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '灌溉单元编号',
                         `fieldSize` double NOT NULL COMMENT '灌溉面积',
                         `fieldRange` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '四个点经纬度信息（json数组）',
                         `groupId` bigint DEFAULT NULL COMMENT '组id',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `task` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                        `fieldId` varchar(255) NOT NULL COMMENT '地块的编号',
                        `fieldUnitId` varchar(255) NOT NULL COMMENT '灌溉单元编号',
                        `taskId` varchar(255) NOT NULL COMMENT '灌溉任务编号',
                        `water` float NOT NULL COMMENT '需水量（单位：m3）',
                        `fertilizerN` float NOT NULL COMMENT '需氮肥量（单位：kg）',
                        `fertilizerP` float NOT NULL COMMENT '需磷肥量（单位：kg）',
                        `fertilizerK` float NOT NULL COMMENT '需钾肥量（单位：kg）',
                        `startTime` datetime NOT NULL COMMENT '作业的开始时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `group_manager` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                 `groupName` varchar(255) NOT NULL COMMENT '组名称',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `field_group` (
                               `fieldId` bigint NOT NULL COMMENT '地块id',
                               `groupId` bigint NOT NULL COMMENT '分组id',
                               UNIQUE KEY `field_groupId_index` (`fieldId`,`groupId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;