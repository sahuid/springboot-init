package com.sahuid.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.model.entity.Argument;
import com.sahuid.springbootinit.service.ArgumentService;
import com.sahuid.springbootinit.mapper.ArgumentMapper;
import org.springframework.stereotype.Service;

/**
* @author wxb
* @description 针对表【argument】的数据库操作Service实现
* @createDate 2025-04-07 22:52:09
*/
@Service
public class ArgumentServiceImpl extends ServiceImpl<ArgumentMapper, Argument>
    implements ArgumentService{

}




