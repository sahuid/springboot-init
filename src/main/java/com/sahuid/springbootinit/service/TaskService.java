package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.task.AddTaskInfoRequest;
import com.sahuid.springbootinit.model.req.task.QueryTaskByPage;

/**
* @author wxb
* @description 针对表【task】的数据库操作Service
* @createDate 2025-03-10 00:08:44
*/
public interface TaskService extends IService<Task> {

    /**
     * 添加任务信息
     * @param addFieldInfoRequest
     */
    void addTaskInfo(AddTaskInfoRequest addTaskInfoRequest);

    /**
     * 分页查询任务信息
     * @param queryTaskByPage
     * @return
     */
    Page<Task> queryTaskByPage(QueryTaskByPage queryTaskByPage);
}
