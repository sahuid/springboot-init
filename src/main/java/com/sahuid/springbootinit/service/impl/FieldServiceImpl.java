package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.mapper.FieldMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author wxb
* @description 针对表【field】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class FieldServiceImpl extends ServiceImpl<FieldMapper, Field>
    implements FieldService{

    @Override
    public void addFieldInfo(AddFieldInfoRequest addFieldInfoRequest) {
        String fieldUnitId = addFieldInfoRequest.getFieldUnitId();
        String fieldId = addFieldInfoRequest.getFieldId();
        String fieldRange = addFieldInfoRequest.getFieldRange();
        if (StringUtils.isAnyBlank(fieldId, fieldUnitId, fieldRange)) {
            throw new RequestParamException("请求参数缺失");
        }
        Field field = new Field();
        BeanUtil.copyProperties(addFieldInfoRequest, field, false);
        boolean save = this.save(field);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<Field> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest) {
        int currPage = queryFieldByPageRequest.getPage();
        int pageSize = queryFieldByPageRequest.getPageSize();
        Page<Field> page = new Page<>(currPage, pageSize);
        this.page(page);
        return page;
    }
}




