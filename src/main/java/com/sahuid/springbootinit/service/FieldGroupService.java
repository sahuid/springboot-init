package com.sahuid.springbootinit.service;

import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.FieldGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.entity.GroupManager;

import java.util.List;

/**
* @author wxb
* @description 针对表【field_group】的数据库操作Service
* @createDate 2025-03-17 16:15:56
*/
public interface FieldGroupService extends IService<FieldGroup> {

    /**
     * 删除组同时删除关联土地信息
     * @param groupManager
     */
    void deleteGroup(GroupManager groupManager);


    /**
     * 查询组关联的地块
     * @param groupManager
     * @return
     */
    List<Field> queryGroupMappingFieldUnitList(GroupManager groupManager);
}
