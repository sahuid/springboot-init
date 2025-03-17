package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.task.AddTaskInfoRequest;
import com.sahuid.springbootinit.model.req.task.QueryTaskByPage;
import com.sahuid.springbootinit.service.TaskService;
import com.sahuid.springbootinit.mapper.TaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author wxb
* @description 针对表【task】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService{

    @Override
    public void addTaskInfo(AddTaskInfoRequest addTaskInfoRequest) {
        Double fertilizerK = addTaskInfoRequest.getFertilizerK();
        Double water = addTaskInfoRequest.getWater();
        Double fertilizerN = addTaskInfoRequest.getFertilizerN();
        String fieldId = addTaskInfoRequest.getFieldId();
        String taskId = addTaskInfoRequest.getTaskId();
        Double fertilizerP = addTaskInfoRequest.getFertilizerP();
        String fieldUnitId = addTaskInfoRequest.getFieldUnitId();
        Date startTime = addTaskInfoRequest.getStartTime();
        if (StringUtils.isAnyBlank(fieldId, taskId, fieldUnitId)) {
            throw new RequestParamException("请求参数缺失");
        }

        if (fertilizerK == null || water == null || fertilizerN == null || fertilizerP == null) {
            throw new RequestParamException("需求量参数缺失");
        }

        if (ObjectUtil.isNull(startTime)) {
            throw new RequestParamException("开始时间缺失");
        }
        Task task = new Task();
        BeanUtil.copyProperties(addTaskInfoRequest, task, false);
        boolean save = this.save(task);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<Task> queryTaskByPage(QueryTaskByPage queryTaskByPage) {
        int currPage = queryTaskByPage.getPage();
        int pageSize = queryTaskByPage.getPageSize();
        Page<Task> page = new Page<>(currPage, pageSize);
        this.page(page);
        return page;
    }
}




