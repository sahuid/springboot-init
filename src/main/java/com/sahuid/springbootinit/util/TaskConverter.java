package com.sahuid.springbootinit.util;

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

    public static List<Task> convertToEntities(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TaskRequestDTO request = mapper.readValue(json, TaskRequestDTO.class);

        if (!"saveTask".equals(request.getType())) {
            throw new IllegalArgumentException("不支持的任务类型");
        }

        TaskRequestDTO.TaskDataDTO data = request.getData();
        List<Task> tasks = new ArrayList<>();

        // 为每个fieldUnitId创建独立任务记录
        for (String unitId : data.getFieldUnitIds()) {
            Task task = new Task();
            task.setTaskId(data.getTaskId());
            task.setFieldId(data.getFieldId());
            task.setFieldUnitId(unitId);
            task.setWater(data.getWater());
            task.setFertilizerN(data.getFertilizerN());
            task.setFertilizerP(data.getFertilizerP());
            task.setFertilizerK(data.getFertilizerK());
            
            try {
                task.setStartTime(DATE_FORMAT.parse(data.getStartTime()));
            } catch (ParseException e) {
                throw new IllegalArgumentException("时间格式必须为 yyyy-MM-dd HH:mm:ss");
            }
            
            tasks.add(task);
        }

        return tasks;
    }
}