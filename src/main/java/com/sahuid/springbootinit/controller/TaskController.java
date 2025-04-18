package com.sahuid.springbootinit.controller;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.req.task.AddTaskInfoRequest;
import com.sahuid.springbootinit.model.req.task.QueryTaskByPage;
import com.sahuid.springbootinit.model.req.task.TaskFieldUnitsToGroupRequest;
import com.sahuid.springbootinit.model.req.task.UpdateTaskByIdRequest;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:42
 **/
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Resource
    private FieldService fieldService;

    @PostMapping("/add")
    public R<Void> addTaskInfo(@RequestBody AddTaskInfoRequest addFieldInfoRequest) {
        taskService.addTaskInfo(addFieldInfoRequest);
        return R.ok("保存成功");
    }

    @GetMapping("/query/page")
    public R<Page<Task>> queryTaskInfoByPage(QueryTaskByPage queryTaskByPage) {
        Page<Task> page = taskService.queryTaskByPage(queryTaskByPage);
        return R.ok(page, "查询成功");
    }

    @PutMapping("/update")
    public R<Void> updateFieldById(@RequestBody UpdateTaskByIdRequest updateTaskByIdRequest) {
        taskService.updateTaskById(updateTaskByIdRequest);
        return R.ok("修改成功");
    }

    @DeleteMapping("/delete")
    public R<Void> deleteFieldById(Long taskId) {
        taskService.deleteTaskById(taskId);
        return R.ok("删除成功");
    }

    @GetMapping("/list")
    public R<List<Task>> queryList() {
        List<Task> list = taskService.list();
        return R.ok(list);
    }

    @PostMapping("/to/group")
    public R<Void> taskFieldUnitsToGroup(@RequestBody TaskFieldUnitsToGroupRequest taskFieldUnitsToGroupRequest) {
        Long groupId = taskFieldUnitsToGroupRequest.getGroupId();
        Long taskId = taskFieldUnitsToGroupRequest.getTaskId();

        Task task = taskService.getById(taskId);
        String jsonUnitList = task.getFieldUnitId();
        List<String> fieldUnitIds = JSONUtil.toBean(jsonUnitList, new TypeReference<>() {
        }, false);
        LambdaQueryWrapper<Field> fieldLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fieldLambdaQueryWrapper.in(Field::getFieldUnitId, fieldUnitIds);
        List<Field> fieldList = fieldService.list(fieldLambdaQueryWrapper);
        for (Field field : fieldList) {
            field.setGroupId(groupId);
        }
        fieldService.updateBatchById(fieldList);
        return R.ok("分组成功");
    }
}
