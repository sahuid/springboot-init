package com.sahuid.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.model.entity.Diff;
import com.sahuid.springbootinit.service.DiffService;
import com.sahuid.springbootinit.mapper.DiffMapper;
import org.springframework.stereotype.Service;

/**
* @author mcj
* @description 针对表【diff】的数据库操作Service实现
* @createDate 2025-05-08 16:10:52
*/
@Service
public class DiffServiceImpl extends ServiceImpl<DiffMapper, Diff>
    implements DiffService{

}




