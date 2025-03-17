package com.sahuid.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.FieldGroup;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.service.FieldGroupService;
import com.sahuid.springbootinit.mapper.FieldGroupMapper;
import com.sahuid.springbootinit.service.FieldService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author wxb
* @description 针对表【field_group】的数据库操作Service实现
* @createDate 2025-03-17 16:15:56
*/
@Service
public class FieldGroupServiceImpl extends ServiceImpl<FieldGroupMapper, FieldGroup>
    implements FieldGroupService{

    @Resource
    private FieldService fieldService;

    @Override
    public void deleteGroup(GroupManager groupManager) {
        if (groupManager == null) {
            throw new RequestParamException("组信息不能为空");
        }
        Long groupId = groupManager.getId();
        LambdaQueryWrapper<FieldGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldGroup::getGroupId, groupId);
        boolean remove = this.remove(wrapper);
        if (!remove) {
            throw new RuntimeException("删除组失败");
        }
    }

    @Override
    public List<Field> queryGroupMappingFieldIdList(GroupManager groupManager) {
        if (groupManager == null) {
            throw new RequestParamException("组信息不能为空");
        }
        Long groupId = groupManager.getId();
        LambdaQueryWrapper<FieldGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldGroup::getGroupId, groupId);
        // 获取组关联信息
        List<FieldGroup> list = this.list(wrapper);
        // 获取对应的地块id
        List<Long> fieldIdList = list.stream()
                .map(FieldGroup::getFieldId)
                .collect(Collectors.toList());
        if (fieldIdList.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Field> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Field::getId, fieldIdList);
        return fieldService.list(lambdaQueryWrapper);
    }
}




