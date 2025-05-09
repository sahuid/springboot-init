package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Diff;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.task.AddTaskInfoRequest;
import com.sahuid.springbootinit.model.req.task.QueryTaskByPage;
import com.sahuid.springbootinit.model.req.task.UpdateTaskByIdRequest;
import com.sahuid.springbootinit.model.vo.TaskVo;
import com.sahuid.springbootinit.service.DiffService;
import com.sahuid.springbootinit.service.TaskService;
import com.sahuid.springbootinit.mapper.TaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author wxb
* @description 针对表【task】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService{

    @Resource
    private DiffService diffService;

    @Override
    public void addTaskInfo(AddTaskInfoRequest addTaskInfoRequest) {
        Double fertilizerK = addTaskInfoRequest.getFertilizerK();
        Double water = addTaskInfoRequest.getWater();
        Double fertilizerN = addTaskInfoRequest.getFertilizerN();
        String fieldId = addTaskInfoRequest.getFieldId();
        String taskId = addTaskInfoRequest.getTaskId();
        Double fertilizerP = addTaskInfoRequest.getFertilizerP();
        List<String> fieldUnitIds = addTaskInfoRequest.getFieldUnitIds();
        Date startTime = addTaskInfoRequest.getStartTime();
        if (StringUtils.isAnyBlank(fieldId, taskId)) {
            throw new RequestParamException("请求参数缺失");
        }
        if (fieldUnitIds.isEmpty()) {
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
        String unitJson = JSONUtil.toJsonStr(fieldUnitIds);
        task.setFieldUnitId(unitJson);
        boolean save = this.save(task);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<TaskVo> queryTaskByPage(QueryTaskByPage queryTaskByPage) {
        int currPage = queryTaskByPage.getPage();
        int pageSize = queryTaskByPage.getPageSize();
        String taskId1 = queryTaskByPage.getTaskId();
        String keyword = queryTaskByPage.getKeyword();
        String startTime = queryTaskByPage.getStartTime();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isBlank(keyword)) {
            wrapper.like(Task::getFieldId, keyword)
                    .or()
                    .like(Task::getTaskId, keyword);
        }
        if (!StringUtils.isBlank(taskId1)) {
            wrapper.eq(Task::getTaskId, taskId1);
        }
        if (!StringUtils.isBlank(startTime)) {
            wrapper.like(Task::getStartTime, startTime);
        }
        Page<Task> page = new Page<>(currPage, pageSize);
        this.page(page, wrapper);
        // 处理分页
        List<Task> records = page.getRecords();
        List<TaskVo> collect = records.stream().map(task -> {
            Integer type = task.getType();
            TaskVo taskVo = new TaskVo();
            BeanUtil.copyProperties(task, taskVo, false);
            if (type == 1) {
                Long taskId = task.getId();
                String fieldUnitJson = task.getFieldUnitId();
                List<String> fieldUnitIds = JSONUtil.toBean(fieldUnitJson, new TypeReference<>() {
                }, false);
                LambdaQueryWrapper<Diff> diffLambdaQueryWrapper = new LambdaQueryWrapper<>();
                diffLambdaQueryWrapper.eq(Diff::getTaskId, taskId);
                diffLambdaQueryWrapper.in(Diff::getFieldUnitId, fieldUnitIds);
                List<Diff> list = diffService.list(diffLambdaQueryWrapper);
                taskVo.setDiffList(list);
            }
            return taskVo;
        }).collect(Collectors.toList());
        Page<TaskVo> taskVoPage = new Page<>(currPage, pageSize);
        BeanUtil.copyProperties(page, taskVoPage, false);
        taskVoPage.setRecords(collect);
        return taskVoPage;
    }

    @Override
    public void updateTaskById(UpdateTaskByIdRequest updateTaskByIdRequest) {
        Long taskId = updateTaskByIdRequest.getId();
        if (taskId == null) {
            throw new RequestParamException("请求参数错误");
        }
        Task task = this.getById(taskId);
        if (task == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateTaskByIdRequest, task, false);
        boolean update = this.updateById(task);
        if (!update) {
            throw new RuntimeException("数据修改失败");
        }
    }

    @Override
    public void deleteTaskById(Long taskId) {
        if (taskId == null) {
            throw new RequestParamException("请求参数缺失");
        }
        boolean remove = this.removeById(taskId);
        if (!remove){
            throw new RuntimeException("删除失败");
        }
    }

    @Override
    public void addDiffTaskInfo(AddTaskInfoRequest addTaskInfoRequest) {
        String fieldId = addTaskInfoRequest.getFieldId();
        String taskId = addTaskInfoRequest.getTaskId();
        List<String> fieldUnitIds = addTaskInfoRequest.getFieldUnitIds();
        Date startTime = addTaskInfoRequest.getStartTime();
        if (StringUtils.isAnyBlank(fieldId, taskId)) {
            throw new RequestParamException("请求参数缺失");
        }
        if (fieldUnitIds.isEmpty()) {
            throw new RequestParamException("请求参数缺失");
        }
        if (ObjectUtil.isNull(startTime)) {
            throw new RequestParamException("开始时间缺失");
        }
        Task task = new Task();
        BeanUtil.copyProperties(addTaskInfoRequest, task, false);
        String unitJson = JSONUtil.toJsonStr(fieldUnitIds);
        task.setFieldUnitId(unitJson);
        task.setType(1);
        boolean save = this.save(task);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }
}




