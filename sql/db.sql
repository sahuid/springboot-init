
CREATE DATABASE init_db;


CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                        `userName` varchar(255) NOT NULL COMMENT '用户名称',
                        `userAccount` varchar(255) NOT NULL COMMENT '用户账号',
                        `userPassword` varchar(1024) NOT NULL COMMENT '用户密码',
                        `userPhone` char(11) DEFAULT NULL COMMENT '用户手机号',
                        `userRole` tinyint(3) unsigned zerofill NOT NULL COMMENT '用户权限: 0:普通用户/1:管理员',
                        `userPicture` varchar(1024) DEFAULT NULL COMMENT '用户头像',
                        `createTime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updateTime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `isDelete` tinyint NOT NULL COMMENT '逻辑删除',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;