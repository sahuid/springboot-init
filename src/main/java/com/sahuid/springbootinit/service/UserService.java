package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.dto.user.UserLoginDto;
import com.sahuid.springbootinit.model.dto.user.UserQueryDto;
import com.sahuid.springbootinit.model.dto.user.UserRegisterDto;
import com.sahuid.springbootinit.model.dto.user.UserUpdateDto;
import com.sahuid.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【user】的数据库操作Service
* @createDate 2024-12-11 11:35:19
*/
public interface UserService extends IService<User> {

    R<UserVo> userLogin(UserLoginDto userLoginDto, HttpServletRequest request);

    R<Void> userRegister(UserRegisterDto userRegisterDto);

    R<Void> userUpdate(UserUpdateDto userUpdateDto);

    R<UserVo> getCurrentUser(HttpServletRequest request);

    R<Page<User>> queryUserByPage(UserQueryDto userQueryDto);
}
