package com.sahuid.springbootinit.util;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahuid.springbootinit.model.dto.task.TaskRequestDTO;
import com.sahuid.springbootinit.model.entity.Task;
import org.springframework.expression.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TaskConverter {

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Task convertToEntities(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TaskRequestDTO request = mapper.readValue(json, TaskRequestDTO.class);

        if (!"saveTask".equals(request.getType())) {
            throw new IllegalArgumentException("不支持的任务类型");
        }

        TaskRequestDTO.TaskDataDTO data = request.getData();
        Task task = new Task();
        task.setTaskId(data.getTaskId());
        task.setFieldId(data.getFieldId());
        task.setFieldUnitId(JSONUtil.toJsonStr(data.getFieldUnitIds()));
        task.setWater(data.getWater());
        task.setFertilizerN(data.getFertilizerN());
        task.setFertilizerP(data.getFertilizerP());
        task.setFertilizerK(data.getFertilizerK());

        try {
            task.setStartTime(DATE_FORMAT.parse(data.getStartTime()));
        } catch (ParseException e) {
            throw new IllegalArgumentException("时间格式必须为 yyyy-MM-dd HH:mm:ss");
        }

        return task;
    }
}