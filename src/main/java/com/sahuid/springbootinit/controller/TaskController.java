package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.req.task.AddTaskInfoRequest;
import com.sahuid.springbootinit.model.req.task.QueryTaskByPage;
import com.sahuid.springbootinit.model.req.task.UpdateTaskByIdRequest;
import com.sahuid.springbootinit.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
}
