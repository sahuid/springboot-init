package com.sahuid.springbootinit.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class UserUpdateDto {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名称
     */
    private String userName;


    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户手机号
     */
    private String userPhone;

}
