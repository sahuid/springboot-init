package com.sahuid.springbootinit.model.dto.user;

import lombok.Data;

@Data
public class UserLoginDto {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;
}
