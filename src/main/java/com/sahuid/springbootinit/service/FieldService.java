package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.Field;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;

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

    /**
     * 根据 id 修改地块信息
     * @param updateFieldByIdRequest
     */
    void updateFieldById(UpdateFieldByIdRequest updateFieldByIdRequest);

    /**
     * 根据 id 删除地块信息
     * @param fieldId
     */
    void deleteFieldById(Long fieldId);
}
