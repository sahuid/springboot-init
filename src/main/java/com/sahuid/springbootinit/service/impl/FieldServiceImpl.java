package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.FieldGroup;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.entity.Locational;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.AddFieldToGroupRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.vo.FieldVO;
import com.sahuid.springbootinit.service.FieldGroupService;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.mapper.FieldMapper;
import com.sahuid.springbootinit.service.GroupManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author wxb
* @description 针对表【field】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class FieldServiceImpl extends ServiceImpl<FieldMapper, Field>
    implements FieldService{

    @Resource
    @Lazy
    private GroupManagerService groupManagerService;

    @Resource
    @Lazy
    private FieldGroupService fieldGroupService;

    @Override
    public void addFieldInfo(AddFieldInfoRequest addFieldInfoRequest) {
        String fieldId = addFieldInfoRequest.getFieldId();
        List<Locational> fieldRange = addFieldInfoRequest.getFieldRange();
        Double fieldSize = addFieldInfoRequest.getFieldSize();
        if (StringUtils.isAnyBlank(fieldId)) {
            throw new RequestParamException("请求参数缺失");
        }
        if (fieldSize == null || fieldRange == null) {
            throw new RequestParamException("请求参数缺失");
        }
        Field field = new Field();
        BeanUtil.copyProperties(addFieldInfoRequest, field, false);
        String jsonStrRange = JSONUtil.toJsonStr(fieldRange);
        field.setFieldRange(jsonStrRange);
        boolean save = this.save(field);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<FieldVO> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest) {
        int currPage = queryFieldByPageRequest.getPage();
        int pageSize = queryFieldByPageRequest.getPageSize();
        Page<Field> page = new Page<>(currPage, pageSize);
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Field::getFieldParent);
        this.page(page,wrapper);
        // 转化为 vo
        Page<FieldVO> voPage = new Page<>();
        BeanUtil.copyProperties(page, voPage, false);
        // 获取基本灌溉单元
        List<Field> records = page.getRecords();

        List<FieldVO> voList = records.stream().map(field -> {
            Long fieldId = field.getId();
            wrapper.clear();
            wrapper.eq(Field::getFieldParent, fieldId);
            List<Field> fieldList = this.list(wrapper);
            FieldVO fieldVO = new FieldVO();
            BeanUtil.copyProperties(field, fieldVO);
            fieldVO.setSubField(fieldList);
            return fieldVO;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void updateFieldById(UpdateFieldByIdRequest updateFieldByIdRequest) {
        Long fieldId = updateFieldByIdRequest.getId();
        List<Locational> fieldRange = updateFieldByIdRequest.getFieldRange();
        if (fieldId == null) {
            throw new RequestParamException("请求参数错误");
        }
        Field field = this.getById(fieldId);
        if (field == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateFieldByIdRequest, field, false);
        if (fieldRange != null) {
            String jsonStr = JSONUtil.toJsonStr(fieldRange);
            field.setFieldRange(jsonStr);
        }
        boolean update = this.updateById(field);
        if (!update) {
            throw new RuntimeException("数据修改失败");
        }
    }

    @Override
    public void deleteFieldById(Long fieldId) {
        Field field = this.getById(fieldId);
        Long fieldParent = field.getFieldParent();
        if (fieldParent == null) {
            LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Field::getFieldParent, field.getId());
            this.remove(wrapper);
        }
        this.removeById(fieldId);
    }

    @Override
    public void addFieldToGroup(AddFieldToGroupRequest addFieldToGroupRequest) {
        Long groupId = addFieldToGroupRequest.getGroupId();
        List<Long> fieldIds = addFieldToGroupRequest.getFieldId();
        if (groupId == null || fieldIds.isEmpty()) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = groupManagerService.getById(groupId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("组信息为空");
        }
        fieldIds.forEach(fieldId -> {
            Field field = this.getById(fieldId);
            if (field == null) {
                throw new DataBaseAbsentException("土地信息为空");
            }
            field.setGroupId(groupId);
            boolean update = this.updateById(field);
            if (!update) {
                throw new RuntimeException("添加组失败");
            }
        });

    }

    @Override
    public List<Field> queryFieldNoGroupList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Field::getFieldParent);
        wrapper.isNull(Field::getGroupId);
        return this.list(wrapper);
    }

    @Override
    public List<Field> queryFieldList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Field::getFieldParent);
        return this.list(wrapper);
    }

    @Override
    public List<Field> queryFieldUnitList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Field::getFieldParent);
        return this.list(wrapper);
    }
}




