package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.Field;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;

/**
* @author wxb
* @description 针对表【field】的数据库操作Service
* @createDate 2025-03-10 00:08:44
*/
public interface FieldService extends IService<Field> {

    /**
     * 添加土地信息
     * @param addFieldInfoRequest
     */
    void addFieldInfo(AddFieldInfoRequest addFieldInfoRequest);

    /**
     * 分页查询土地信息
     * @param queryFieldByPageRequest
     * @return
     */
    Page<Field> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest);
}
