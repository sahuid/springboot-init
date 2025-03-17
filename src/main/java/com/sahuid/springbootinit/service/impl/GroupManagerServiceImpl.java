package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.req.group.AddGroupInfoRequest;
import com.sahuid.springbootinit.model.req.group.QueryGroupByPageRequest;
import com.sahuid.springbootinit.model.req.group.UpdateGroupByIdRequest;
import com.sahuid.springbootinit.model.vo.GroupVo;
import com.sahuid.springbootinit.service.FieldGroupService;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.service.GroupManagerService;
import com.sahuid.springbootinit.mapper.GroupManagerMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author wxb
* @description 针对表【group_manager】的数据库操作Service实现
* @createDate 2025-03-17 15:54:49
*/
@Service
public class GroupManagerServiceImpl extends ServiceImpl<GroupManagerMapper, GroupManager>
    implements GroupManagerService{

    @Resource
    private FieldGroupService fieldGroupService;

    @Resource
    private FieldService fieldService;

    @Override
    public void addGroupInfo(AddGroupInfoRequest addGroupInfoRequest) {
        String groupName = addGroupInfoRequest.getGroupName();
        if (StrUtil.isBlank(groupName)) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = new GroupManager();
        groupManager.setGroupName(groupName);
        boolean save = this.save(groupManager);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<GroupVo> queryGroupInfoByPage(QueryGroupByPageRequest queryGroupByPageRequest) {
        int currPage = queryGroupByPageRequest.getPage();
        int pageSize = queryGroupByPageRequest.getPageSize();
        Page<GroupManager> page = new Page<>(currPage, pageSize);
        this.page(page);
        List<GroupManager> records = page.getRecords();
        List<GroupVo> collect = records.stream()
                .map(this::getGroupVo)
                .collect(Collectors.toList());
        Page<GroupVo> groupVoPage = new Page<>();
        BeanUtil.copyProperties(page, groupVoPage);
        groupVoPage.setRecords(collect);
        return groupVoPage;
    }

    @NotNull
    private GroupVo getGroupVo(GroupManager groupManager) {
        GroupVo groupVo = new GroupVo();
        BeanUtil.copyProperties(groupManager, groupVo, false);
        List<Field> fields = fieldGroupService.queryGroupMappingFieldIdList(groupManager);
        groupVo.setFieldList(fields);
        return groupVo;
    }

    @Override
    public void updateGroupById(UpdateGroupByIdRequest updateGroupByIdRequest) {
        Long userId = updateGroupByIdRequest.getId();
        if (userId == null) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = this.getById(userId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateGroupByIdRequest, groupManager, false);
        boolean update = this.updateById(groupManager);
        if (!update) {
            throw new RuntimeException("修改失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupById(Long groupId) {
        if (groupId == null) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = this.getById(groupId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("组不存在");
        }
        boolean update = this.removeById(groupId);
        // 删除 组 和 地块的关联信息
        fieldGroupService.deleteGroup(groupManager);
        if (!update) {
            throw new RuntimeException("组信息删除失败");
        }
    }

    @Override
    public List<GroupManager> queryGroupList() {
        return this.list();
    }
}




