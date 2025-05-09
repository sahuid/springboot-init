package com.sahuid.springbootinit.util;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahuid.springbootinit.model.dto.task.IrrigationTaskDTO;
import com.sahuid.springbootinit.model.dto.task.TaskRequestDTO;
import com.sahuid.springbootinit.model.entity.Diff;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.service.DiffService;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.service.TaskService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class TaskConverter {

    @Resource
    private TaskService taskService;

    @Resource
    private DiffService diffService;

    @Resource
    private FieldService fieldService;

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public  Task convertToEntities(String json) throws ParseException, JsonProcessingException {
        DATE_FORMAT.setTimeZone(TimeZone.getDefault());

        ObjectMapper mapper = new ObjectMapper();
        IrrigationTaskDTO taskDTO  = mapper.readValue(json, IrrigationTaskDTO.class);
        Integer type = taskDTO.getData().getType();

        // 保存主任务
        Task task = new Task();
        task.setFieldId(taskDTO.getData().getFieldId());
        task.setTaskId(taskDTO.getData().getTaskId());
        //String format = DATE_FORMAT.format(taskDTO.getData().getStartTime());
        Date parse = DATE_FORMAT.parse(taskDTO.getData().getStartTime());

        task.setStartTime(parse);
        task.setType(taskDTO.getData().getType());
        task.setFieldUnitId(taskDTO.getData().getFieldUnitId());
        if (type == 0) {
            IrrigationTaskDTO.UnitParam unitParam = taskDTO.getUnitParams().get(0);
            task.setWater(unitParam.getWater());
            task.setFertilizerK(unitParam.getFertilizerK());
            task.setFertilizerN(unitParam.getFertilizerN());
            task.setFertilizerP(unitParam.getFertilizerP());
        }

        taskService.save(task);
        Long taskId = task.getId();

        if (type == 1) {
            List<Diff> units = taskDTO.getUnitParams().stream()
                    .map(param -> {
                        Diff unit = new Diff();
                        unit.setTaskId(taskId);
                        unit.setFieldUnitId(param.getFieldUnitId());
                        unit.setWater(param.getWater());
                        unit.setFertilizerN(param.getFertilizerN());
                        unit.setFertilizerP(param.getFertilizerP());
                        unit.setFertilizerK(param.getFertilizerK());
                        return unit;
                    })
                    .collect(Collectors.toList());

            diffService.saveBatch(units);
        }
        return task;
    }
}