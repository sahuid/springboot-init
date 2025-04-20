package com.sahuid.springbootinit.job;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sahuid.springbootinit.model.entity.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
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

    /**
     * 整体顺序肥
     * @param field
     * @param groupManager
     * @param taps
     * @param pump
     * @param fertilizerApplicator
     * @param rangeSize
     * @param task
     * @param argument
     * @param sortType
     * @return
     */
    public String workOrder(Field field, GroupManager groupManager,
                            List<Device> taps, Device pump, Device fertilizerApplicator,
                            Double rangeSize, Task task, Argument argument, List<Integer> sortType) {
        JSONObject resultObject = new JSONObject(true);
        // 地块编号
        resultObject.set("fieldId", field.getFieldId());
        // 灌溉作业时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dateTime", formattedDate);
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
        double T1 = rangeSize * headWaterConsumption / currentSpeed;
        long t1Timebase = baseTime + Math.round(T1 * 1000);
        String t1Time = sdf.format(t1Timebase);
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
        // 绝对时间
        JSONObject t1TimeJson = new JSONObject(true);
        t1TimeJson.set("control_shiftTime", t1Time);
        JSONArray control = new JSONArray();
        control.add(t1TimeJson);
        control.add(t1TapsController);
        control.add(t1PumpController);
        t1JSONObject.set("control_Seq", control);

        // T2 补偿
        JSONObject t2JSONObject = new JSONObject(true);
        jsonArray.add(t2JSONObject);
        Double fertilizerK = task.getFertilizerK();
        Double fertilizerN = task.getFertilizerN();
        Double fertilizerP = task.getFertilizerP();
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        Double water = task.getWater();
        Double tailWaterConsumption = argument.getTail_water_consumption();
        double T2 = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP))) / currentSpeed;
        long t2Timebase = baseTime + Math.round(T2 * 1000);
        String t2Time = sdf.format(t2Timebase);
        t2JSONObject.set("irrigation_type", 0);
        Double t2Water = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP)));
        t2JSONObject.set("irrigation_Water", t2Water);
        // 控制顺序
        JSONArray t2Controller = new JSONArray();
        JSONObject t2TimeJson = new JSONObject(true);
        t2TimeJson.set("control_shiftTime", t2Time);
        t2Controller.add(t2TimeJson);
        t2JSONObject.set("control_Seq", t2Controller);


        // 单质肥添加
        for (Integer sortTypeKey : sortType) {
            JSONObject jsonObject = new JSONObject(true);
            jsonArray.add(jsonObject);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)){
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            }else if ("钾".equals(type)){
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            }else {
                number = task.getFertilizerP();
                jsonObject.set("irrigation_type", 3);
                jsonObject.set("irrigation_P", number);
            }
            double T3 = waterAndFertilizer * number / currentSpeed;
            long t3Timebase = baseTime + Math.round(T3 * 1000);
            String t3Time = sdf.format(t3Timebase);
            Double t3Water = waterAndFertilizer * number;
            jsonObject.set("irrigation_Water", t3Water);

            // 控制顺序
            JSONObject t3Controller = new JSONObject(true);
            t3Controller.set("controled_Object_Type", 4);
            if (fertilizerApplicator != null) {
                t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
            }
            t3Controller.set("controled_Cmd", 1);
            t3Controller.set("control_shiftTime", t3Time);
            jsonObject.set("control_Seq", t3Controller);
        }

        //t4 结束
        JSONObject t4JSONObject = new JSONObject(true);
        jsonArray.add(t4JSONObject);
        t4JSONObject.set("irrigation_type", 0);
        double T4 = rangeSize * tailWaterConsumption / currentSpeed;
        long t4Timebase = baseTime + Math.round(T4 * 1000);
        String t4Time = sdf.format(t4Timebase);
        Double t4Water = rangeSize * tailWaterConsumption;
        t4JSONObject.set("irrigation_Water", t4Water);

        // 控制顺序
        JSONObject tailT1PumpController = JSONUtil.parseObj(t1PumpController.toString());
        tailT1PumpController.set("controled_Cmd", 0);
        JSONObject tailT1TapsController = JSONUtil.parseObj(t1TapsController.toString());
        tailT1TapsController.set("controled_Cmd", 0);
        JSONObject t3Controller = new JSONObject(true);
        t3Controller.set("controled_Object_Type", 4);
        if (fertilizerApplicator != null) {
            t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
        }
        t3Controller.set("controled_Cmd", 0);
        // 绝对时间
        JSONObject t4TimeJson = new JSONObject(true);
        t4TimeJson.set("control_shiftTime", t4Time);
        JSONArray t4Controller = new JSONArray();
        t4Controller.add(t4TimeJson);
        t4Controller.add(tailT1PumpController);
        t4Controller.add(tailT1TapsController);
        t4Controller.add(t3Controller);

        t4JSONObject.set("control_Seq", t4Controller);

        resultObject.set("scheduling_Seq", jsonArray);
        return JSONUtil.toJsonStr(resultObject);
    }


    /**
     * 整体混合肥
     * @param field
     * @param groupManager
     * @param taps
     * @param pump
     * @param fertilizerApplicator
     * @param rangeSize
     * @param task
     * @param argument
     * @return
     */
    public String workMix(Field field, GroupManager groupManager,
                            List<Device> taps, Device pump, Device fertilizerApplicator,
                            Double rangeSize, Task task, Argument argument) {
        JSONObject resultObject = new JSONObject(true);
        // 地块编号
        resultObject.set("fieldId", field.getFieldId());
        // 灌溉作业时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dateTime", formattedDate);
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
        double T1 = rangeSize * headWaterConsumption / currentSpeed;
        long t1Timebase = baseTime + Math.round(T1 * 1000);
        String t1Time = sdf.format(t1Timebase);
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
        // 绝对时间
        JSONObject t1TimeJson = new JSONObject(true);
        t1TimeJson.set("control_shiftTime", t1Time);
        JSONArray control = new JSONArray();
        control.add(t1TimeJson);
        control.add(t1TapsController);
        control.add(t1PumpController);
        t1JSONObject.set("control_Seq", control);

        // T2 补偿
        JSONObject t2JSONObject = new JSONObject(true);
        jsonArray.add(t2JSONObject);
        Double fertilizerK = task.getFertilizerK();
        Double fertilizerN = task.getFertilizerN();
        Double fertilizerP = task.getFertilizerP();
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        Double water = task.getWater();
        Double tailWaterConsumption = argument.getTail_water_consumption();
        double T2 = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP))) / currentSpeed;
        long t2Timebase = baseTime + Math.round(T2 * 1000);
        String t2Time = sdf.format(t2Timebase);
        t2JSONObject.set("irrigation_type", 0);
        Double t2Water = (water - rangeSize * headWaterConsumption - rangeSize * tailWaterConsumption - (waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP)));
        t2JSONObject.set("irrigation_Water", t2Water);
        // 控制顺序
        JSONArray t2Controller = new JSONArray();
        JSONObject t2TimeJson = new JSONObject(true);
        t2TimeJson.set("control_shiftTime", t2Time);
        t2Controller.add(t2TimeJson);
        t2JSONObject.set("control_Seq", t2Controller);


        // 混合肥添加
        JSONArray mixArray = new JSONArray();
        jsonArray.add(mixArray);
        for (String type : sortTypeMap.values()) {
            JSONObject jsonObject = new JSONObject(true);
            mixArray.add(jsonObject);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)){
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            }else if ("钾".equals(type)){
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            }else {
                number = task.getFertilizerP();
                jsonObject.set("irrigation_type", 3);
                jsonObject.set("irrigation_P", number);
            }
            double T3 = waterAndFertilizer * number / currentSpeed;
            long t3Timebase = baseTime + Math.round(T3 * 1000);
            String t3Time = sdf.format(t3Timebase);
            Double t3Water = waterAndFertilizer * number;
            jsonObject.set("irrigation_Water", t3Water);

            // 控制顺序
            JSONObject t3Controller = new JSONObject(true);
            t3Controller.set("controled_Object_Type", 4);
            if (fertilizerApplicator != null) {
                t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
            }
            t3Controller.set("controled_Cmd", 1);
            t3Controller.set("control_shiftTime", t3Time);
            jsonObject.set("control_Seq", t3Controller);
        }

        //t4 结束
        JSONObject t4JSONObject = new JSONObject(true);
        jsonArray.add(t4JSONObject);
        t4JSONObject.set("irrigation_type", 0);
        double T4 = rangeSize * tailWaterConsumption / currentSpeed;
        long t4Timebase = baseTime + Math.round(T4 * 1000);
        String t4Time = sdf.format(t4Timebase);
        Double t4Water = rangeSize * tailWaterConsumption;
        t4JSONObject.set("irrigation_Water", t4Water);

        // 控制顺序
        JSONObject tailT1PumpController = JSONUtil.parseObj(t1PumpController.toString());
        tailT1PumpController.set("controled_Cmd", 0);
        JSONObject tailT1TapsController = JSONUtil.parseObj(t1TapsController.toString());
        tailT1TapsController.set("controled_Cmd", 0);
        JSONObject t3Controller = new JSONObject(true);
        t3Controller.set("controled_Object_Type", 4);
        if (fertilizerApplicator != null) {
            t3Controller.set("controled_Object_id", fertilizerApplicator.getDeviceId());
        }
        t3Controller.set("controled_Cmd", 0);
        // 绝对时间
        JSONObject t4TimeJson = new JSONObject(true);
        t4TimeJson.set("control_shiftTime", t4Time);
        JSONArray t4Controller = new JSONArray();
        t4Controller.add(t4TimeJson);
        t4Controller.add(tailT1PumpController);
        t4Controller.add(tailT1TapsController);
        t4Controller.add(t3Controller);

        t4JSONObject.set("control_Seq", t4Controller);

        resultObject.set("scheduling_Seq", jsonArray);
        return JSONUtil.toJsonStr(resultObject);
    }
}
