package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.dto.user.UserLoginDto;
import com.sahuid.springbootinit.model.dto.user.UserQueryDto;
import com.sahuid.springbootinit.model.dto.user.UserRegisterDto;
import com.sahuid.springbootinit.model.dto.user.UserUpdateDto;
import com.sahuid.springbootinit.model.entity.User;
import com.sahuid.springbootinit.model.vo.UserVo;
import com.sahuid.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public R<UserVo> userLogin(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        return userService.userLogin(userLoginDto, request);
    }

    @PostMapping("/register")
    public R<Void> userRegister(@RequestBody UserRegisterDto userRegisterDto) {
        return userService.userRegister(userRegisterDto);
    }

    @PostMapping("/update")
    public R<Void> userUpdate(@RequestBody UserUpdateDto userUpdateDto) {
        return userService.userUpdate(userUpdateDto);
    }

    @GetMapping("/me")
    public R<UserVo> getCurrentUser(HttpServletRequest request){
        return userService.getCurrentUser(request);
    }


    @GetMapping("/queryPage")
    public R<Page<User>> queryUserByPage(UserQueryDto userQueryDto){
        return userService.queryUserByPage(userQueryDto);
    }
}
