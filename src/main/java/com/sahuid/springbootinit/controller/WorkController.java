package com.sahuid.springbootinit.controller;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.job.Worker;
import com.sahuid.springbootinit.model.entity.*;
import com.sahuid.springbootinit.model.req.work.WorkRequest;
import com.sahuid.springbootinit.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/9 0:27
 **/
@RestController
@RequestMapping("/work")
public class WorkController {

    @Resource
    private FieldService fieldService;

    @Resource
    private GroupManagerService groupManagerService;

    @Resource
    private TaskService taskService;

    @Resource
    private ArgumentService argumentService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private Worker worker;

    @GetMapping("/work")
    public R<String> workOrder(WorkRequest workRequest) {
        Long fieldId = workRequest.getFieldId();
        Long groupId = workRequest.getGroupId();
        Long taskId = workRequest.getTaskId();
        Long argumentId = workRequest.getArgumentId();
        List<Integer> sortType = workRequest.getSortType();

        Field field = fieldService.getById(fieldId);
        GroupManager groupManager = groupManagerService.getById(groupId);
        // 寻找阀门
        Task task = taskService.getById(taskId);

        String jsonUnitList = task.getFieldUnitId();
        List<String> fieldUnitIds = JSONUtil.toBean(jsonUnitList, new TypeReference<>() {
        }, false);
        LambdaQueryWrapper<Device> deviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceLambdaQueryWrapper.in(Device::getDeviceManagerNumber, fieldUnitIds);
        List<Device> tapsList = deviceService.list(deviceLambdaQueryWrapper);


        // 寻找水泵和施肥机
        deviceLambdaQueryWrapper.clear();
        deviceLambdaQueryWrapper.eq(Device::getDeviceManagerNumber, field.getFieldId());
        List<Device> otherDevice = deviceService.list(deviceLambdaQueryWrapper);
        Device pump = otherDevice.stream().filter(device -> device.getDeviceType() == 1).findFirst().orElse(null);
        Device filter = otherDevice.stream().filter(device -> device.getDeviceType() == 2).findFirst().orElse(null);

        // 计算面积
        LambdaQueryWrapper<Field> fieldLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fieldLambdaQueryWrapper.in(Field::getFieldUnitId, fieldUnitIds);
        List<Field> fieldList = fieldService.list(fieldLambdaQueryWrapper);
        Double sum = 0d;
        for (Field field1 : fieldList) {
            sum += field1.getFieldSize();
        }


        Argument argument = argumentService.getById(argumentId);
        String result = worker.workOrder(field, groupManager, tapsList, pump, filter, sum, task, argument, sortType);
        return R.ok(result, "返回成功");
    }


    @GetMapping("/work/mix")
    public R<String> workMix(WorkRequest workRequest) {
        Long fieldId = workRequest.getFieldId();
        Long groupId = workRequest.getGroupId();
        Long taskId = workRequest.getTaskId();
        Long argumentId = workRequest.getArgumentId();
        List<Integer> sortType = workRequest.getSortType();

        Field field = fieldService.getById(fieldId);
        GroupManager groupManager = groupManagerService.getById(groupId);
        // 寻找阀门
        Task task = taskService.getById(taskId);

        String jsonUnitList = task.getFieldUnitId();
        List<String> fieldUnitIds = JSONUtil.toBean(jsonUnitList, new TypeReference<>() {
        }, false);
        LambdaQueryWrapper<Device> deviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceLambdaQueryWrapper.in(Device::getDeviceManagerNumber, fieldUnitIds);
        List<Device> tapsList = deviceService.list(deviceLambdaQueryWrapper);


        // 寻找水泵和施肥机
        deviceLambdaQueryWrapper.clear();
        deviceLambdaQueryWrapper.eq(Device::getDeviceManagerNumber, field.getFieldId());
        List<Device> otherDevice = deviceService.list(deviceLambdaQueryWrapper);
        Device pump = otherDevice.stream().filter(device -> device.getDeviceType() == 1).findFirst().orElse(null);
        Device filter = otherDevice.stream().filter(device -> device.getDeviceType() == 2).findFirst().orElse(null);

        // 计算面积
        LambdaQueryWrapper<Field> fieldLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fieldLambdaQueryWrapper.in(Field::getFieldUnitId, fieldUnitIds);
        List<Field> fieldList = fieldService.list(fieldLambdaQueryWrapper);
        Double sum = 0d;
        for (Field field1 : fieldList) {
            sum += field1.getFieldSize();
        }
        Argument argument = argumentService.getById(argumentId);
        String result = worker.workMix(field, groupManager, tapsList, pump, filter, sum, task, argument);
        return R.ok(result, "返回成功");
    }
}
