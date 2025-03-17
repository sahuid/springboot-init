package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.group.AddGroupInfoRequest;
import com.sahuid.springbootinit.model.req.group.QueryGroupByPageRequest;
import com.sahuid.springbootinit.model.req.group.UpdateGroupByIdRequest;
import com.sahuid.springbootinit.model.vo.GroupVo;

import java.util.List;

/**
* @author wxb
* @description 针对表【group_manager】的数据库操作Service
* @createDate 2025-03-17 15:54:49
*/
public interface GroupManagerService extends IService<GroupManager> {

    /**
     * 添加 group 信息
     * @param addGroupInfoRequest
     */
    void addGroupInfo(AddGroupInfoRequest addGroupInfoRequest);

    /**
     * 分页查询组信息
     * @param queryGroupByPageRequest
     * @return
     */
    Page<GroupVo> queryGroupInfoByPage(QueryGroupByPageRequest queryGroupByPageRequest);

    /**
     * 根据 id 修改组信息
     * @param updateGroupByIdRequest
     */
    void updateGroupById(UpdateGroupByIdRequest updateGroupByIdRequest);

    /**
     * 根据 id 删除组信息
     * @param groupId
     */
    void deleteGroupById(Long groupId);

    /**
     * 查询所有组信息
     * @return
     */
    List<GroupManager> queryGroupList();
}
