package com.sahuid.springbootinit.job;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sahuid.springbootinit.model.entity.*;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/8 2:31
 **/
@Component
public class Worker {

    Map<Integer, String> sortTypeMap = new HashMap<>();

    {
        sortTypeMap.put(1, "氮");
        sortTypeMap.put(2, "磷");
        sortTypeMap.put(3, "钾");
    }

    public String workerTest(Field field, GroupManager groupManager,
                             List<Device> taps, Device pump, Device fertilizerApplicator,
                             Double rangeSize, Task task, Argument argument, List<Integer> sortType) {
        JSONObject resultObject = new JSONObject(true);
        // 地块编号
        resultObject.set("fieldId", field.getFieldId());
        // 灌溉作业时间
        resultObject.set("dateTime", task.getStartTime());
        // 任务编号
        resultObject.set("taskId", task.getId());
        // 灌溉调度任务
        JSONObject taskObject = new JSONObject(true);
        resultObject.set("scheduleData", taskObject);
        // 编组编号
        taskObject.set("groupId", groupManager.getId());
        // 阀门编号
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            taskObject.set("taps", tapsIdList.toString());
        }
        // 水泵编号
        if (pump != null) {
            taskObject.set("pump", pump.getDeviceId());
        }
        // 施肥机编号
        if (fertilizerApplicator != null) {
            taskObject.set("fertilizer_Applicator", fertilizerApplicator.getDeviceId());
        }
        // T1 头刷
        JSONArray jsonArray = new JSONArray();
        JSONObject t1JSONObject = new JSONObject(true);
        jsonArray.add(t1JSONObject);
        Double headWaterConsumption = argument.getHead_water_consumption();
        Double currentSpeed = argument.getCurrent_speed();
        Double T1 = rangeSize * headWaterConsumption / currentSpeed;
        // t1 灌溉类型
        t1JSONObject.set("irrigation_type", 0);
        // 灌溉量
        Double t1Water = rangeSize * headWaterConsumption;
        t1JSONObject.set("irrigation_Water", t1Water);

        // 控制顺序
        JSONObject t1PumpController = new JSONObject(true);
        t1PumpController.set("controled_Object_Type", 3);
        if (pump != null) {
            t1PumpController.set("controled_Object_id", pump.getDeviceId());
        }
        t1PumpController.set("controled_Cmd", 1);

        JSONObject t1TapsController = new JSONObject(true);
        t1TapsController.set("controled_Object_Type", 2);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            t1TapsController.set("controled_Object_id", tapsIdList.toString());
        }
        t1TapsController.set("controled_Cmd", 1);
        JSONArray control = new JSONArray();
        control.add(t1TapsController);
        control.add(t1PumpController);
        t1JSONObject.set("control_Seq", control);

        // T2 补偿
        JSONObject t2JSONObject = new JSONObject();
        jsonArray.add(t2JSONObject);
        Double fertilizerK = task.getFertilizerK();
        Double fertilizerN = task.getFertilizerN();
        Double fertilizerP = task.getFertilizerP();
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        Double water = task.getWater();
        Double tailWaterConsumption = argument.getTail_water_consumption();
        Double T2 = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP))) / currentSpeed;
        t2JSONObject.set("irrigation_type", 0);
        Double t2Water = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP)));
        t2JSONObject.set("irrigation_Water", t2Water);

        // 单质肥添加
        for (Integer sortTypeKey : sortType) {
            JSONObject jsonObject = new JSONObject(true);
            jsonArray.add(jsonObject);
            String type = sortTypeMap.get(sortTypeKey);
            Double number;
            if ("氮".equals(type)){
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
            }else if ("钾".equals(type)){
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
            }else {
                number = task.getFertilizerP();
                jsonObject.set("irrigation_type", 3);
            }
            Double t3 = waterAndFertilizer * number / currentSpeed;
            Double t3Water = waterAndFertilizer * number;
            jsonObject.set("irrigation_Water", t3Water);

            // 控制顺序
            JSONObject t3Controller = new JSONObject();
            t3Controller.set("controled_Object_Type", 4);
            if (fertilizerApplicator != null) {
                t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
            }
            t3Controller.set("controled_Cmd", 1);
            jsonObject.set("control_Seq", t3Controller);
        }

        //t4 结束
        JSONObject t4JSONObject = new JSONObject();
        jsonArray.add(t4JSONObject);
        t4JSONObject.set("irrigation_type", 0);
        Double T4 = rangeSize * tailWaterConsumption / currentSpeed;
        Double t4Water = rangeSize * tailWaterConsumption;
        t4JSONObject.set("irrigation_Water", t4Water);

        // 控制顺序
        t1PumpController.set("controled_Cmd", 0);
        t1TapsController.set("controled_Cmd", 0);
        JSONObject t3Controller = new JSONObject();
        t3Controller.set("controled_Object_Type", 4);
        if (fertilizerApplicator != null) {
            t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
        }
        t3Controller.set("controled_Cmd", 0);
        JSONArray t4Controller = new JSONArray();
        t4Controller.add(t1PumpController);
        t4Controller.add(t1TapsController);
        t4Controller.add(t3Controller);

        t4JSONObject.set("control_Seq", t4Controller);

        resultObject.set("scheduling_Seq", jsonArray);
        return JSONUtil.toJsonStr(resultObject);
    }
}
