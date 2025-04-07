package com.sahuid.springbootinit.job;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sahuid.springbootinit.model.entity.Task;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.Date;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/8 2:31
 **/
@Component
public class Worker {


    public String workerTest(String fieldId, String fieldUnitId, String rangeSize,
                             Task task, Integer water_and_fertilizer,
                             Double water_consumption, Double current_speed){
        JSONObject resultObject = new JSONObject(true);
        // 地块编号
        resultObject.set("fieldId", fieldId);
        // 灌溉作业时间
        resultObject.set("dateTime", task.getStartTime());
        // 任务编号
        resultObject.set("taskId", task.getId());
        // 灌溉调度任务
        JSONObject taskObject = new JSONObject();
        resultObject.set("scheduleData", taskObject);
        // 编组编号
        taskObject.set("groupId", 1);
        // 阀门编号
        taskObject.set("taps", "[1,2,3]");
        // 水泵编号
        taskObject.set("pump", "p001");
        // 施肥机编号
        taskObject.set("fertilizer_Applicator", "f001");
        // T1
        Double T1 = 0d;//rangeSize *


        return JSONUtil.toJsonStr(resultObject);
    }
}
