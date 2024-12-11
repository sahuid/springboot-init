package com.sahuid.springbootinit.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.sahuid.springbootinit.model.entity.User;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
public class UserVo {


    /**
     * id
     */
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;


    /**
     * 用户权限: 0:普通用户/1:管理员
     */
    private Integer userRole;

    /**
     * 用户头像
     */
    private String userPicture;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public static UserVo userToVo(User user) {
        return UserVo.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .userAccount(user.getUserAccount())
                .userPicture(user.getUserPicture())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
