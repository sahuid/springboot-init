package com.sahuid.springbootinit.job;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     *
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
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            } else {
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

    public String workOrderSpeed(Field field, GroupManager groupManager,
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
        t1TimeJson.set("flow_rate", T1 * currentSpeed);
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
        t2TimeJson.set("flow_rate", T2 * currentSpeed);
        t2Controller.add(t2TimeJson);
        t2JSONObject.set("control_Seq", t2Controller);


        // 单质肥添加
        for (Integer sortTypeKey : sortType) {
            JSONObject jsonObject = new JSONObject(true);
            jsonArray.add(jsonObject);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            } else {
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
            t3Controller.set("flow_rate", T3 * currentSpeed);
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
        t4TimeJson.set("flow_rate", T4 * currentSpeed);
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
     *
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
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            } else {
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

    public String workMixSpeed(Field field, GroupManager groupManager,
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
        t1TimeJson.set("flow_rate", T1 * currentSpeed);
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
        t2TimeJson.set("flow_rate", T2 * currentSpeed);
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
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                jsonObject.set("irrigation_type", 2);
                jsonObject.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                jsonObject.set("irrigation_type", 4);
                jsonObject.set("irrigation_K", number);
            } else {
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
            t3Controller.set("flow_rate", T3 * currentSpeed);
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
        t4TimeJson.set("flow_rate", T4 * currentSpeed);
        JSONArray t4Controller = new JSONArray();
        t4Controller.add(t4TimeJson);
        t4Controller.add(tailT1PumpController);
        t4Controller.add(tailT1TapsController);
        t4Controller.add(t3Controller);

        t4JSONObject.set("control_Seq", t4Controller);

        resultObject.set("scheduling_Seq", jsonArray);
        return JSONUtil.toJsonStr(resultObject);
    }


    public String wordDiff(List<Device> taps, Device pump, Device fertilizerApplicator,
                           List<Field> fieldUnitList, Argument argument, Task task, Field field,
                           GroupManager groupManager, List<Integer> sortType) {
        // 获取流速
        Double currentSpeed = argument.getCurrent_speed();
        // 水肥比
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        // 头耗水量
        Double headWaterConsumption = argument.getHead_water_consumption();
        // 尾耗水量
        Double tailWaterConsumption = argument.getTail_water_consumption();
        // 耗水量
        Double waterNum = task.getWater();
        // 氮肥
        Double fertilizerN = task.getFertilizerN();
        // 磷肥
        Double fertilizerP = task.getFertilizerP();
        // 钾肥
        Double fertilizerK = task.getFertilizerK();
        // 灌溉单元计算数据对应 map
        Map<Field, DiffArgument> fieldDiffArgumentMap = new HashMap<>();
        // 计算总面积
        double sumSize = 0;
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            sumSize += fieldSize;
            // 初始化 map
            fieldDiffArgumentMap.put(field1, new DiffArgument());
        }
        // 计算单位面积流速
        double sizeSpeed = currentSpeed / sumSize;
        // 头纯水阶段时长
        double headWaterTime = headWaterConsumption / sizeSpeed;
        // 尾纯水阶段时长
        double tailWaterTime = tailWaterConsumption / sizeSpeed;
        // 单位面积最大施氮肥量
        double maxNNum = 0d;
        // 单位面积最大施钾肥量
        double maxKNum = 0d;
        // 单位面积最大施磷肥量
        double maxPNum = 0d;
        // 单位纯水最大补偿量
        double maxWaterCompensationNum = 0d;
        // 灌溉单元计算
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            // 需要钾肥量
            double kNum = fertilizerK / fieldSize;
            // 需要氮肥量
            double nNum = fertilizerN / fieldSize;
            // 需要磷肥量
            double pNum = fertilizerP / fieldSize;
            maxNNum = Math.max(maxNNum, nNum);
            maxKNum = Math.max(maxKNum, kNum);
            maxPNum = Math.max(maxPNum, pNum);
            // 单位纯水补偿量
            double waterCompensationNum = waterNum
                    / ((fieldSize - headWaterConsumption - tailWaterConsumption - kNum - nNum - pNum) * waterAndFertilizer);
            maxWaterCompensationNum = Math.max(maxWaterCompensationNum, waterCompensationNum);
            // 填充map参数
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setKNum(kNum);
            diffArgument.setPNum(pNum);
            diffArgument.setNNum(nNum);
            diffArgument.setWaterCompensationNum(waterCompensationNum);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大施氮肥的时间（即施肥机氮肥模式的时间）
        double maxNTime = (maxNNum * waterAndFertilizer) / sizeSpeed;
        // 最大施钾肥的时间（即施肥机钾肥模式的时间）
        double maxKTime = (maxKNum * waterAndFertilizer) / sizeSpeed;
        // 最大施磷肥的时间（即施肥机磷肥模式的时间）
        double maxPTime = (maxPNum * waterAndFertilizer) / sizeSpeed;
        // 除需肥最大单元外的 扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            // 氮肥
            double nDeductionTime = (maxNTime * (maxNNum - fertilizerN)) / maxNNum;
            // 钾肥
            double kDeductionTime = (maxKTime * (maxKNum - fertilizerN)) / maxKNum;
            // 磷肥
            double pDeductionTime = (maxPTime * (maxPNum - fertilizerN)) / maxPNum;
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setNDeductionTime(nDeductionTime);
            diffArgument.setKDeductionTime(kDeductionTime);
            diffArgument.setPDeductionTime(pDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }

        // 补偿时间
        double compensationTime = maxWaterCompensationNum / currentSpeed;
        // 纯水补偿的扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 获取单元单位纯水补偿量
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            // 纯水补偿的扣除时间
            double waterCompensationDeductionTime = (compensationTime * (maxWaterCompensationNum - waterCompensationNum)) / maxWaterCompensationNum;
            diffArgument.setWaterCompensationDeductionTime(waterCompensationDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 计算关闭时间
        double nCloseTime = maxNTime;
        double pCloseTime = maxPTime;
        double kCloseTime = maxKTime;
        double waterCloseTime = maxWaterCompensationNum;
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 计算氮肥关闭时间
            if (diffArgument.getNNum() != maxNNum) {
                nCloseTime -= diffArgument.getNDeductionTime();
            }
            // 计算磷肥关闭时间
            if (diffArgument.getPNum() != maxPNum) {
                pCloseTime -= diffArgument.getPDeductionTime();
            }
            // 计算钾肥关闭时间
            if (diffArgument.getKNum() != maxKNum) {
                kCloseTime -= diffArgument.getKDeductionTime();
            }
            // 计算纯水关闭时间
            if (diffArgument.getWaterCompensationNum() != maxWaterCompensationNum) {
                waterCloseTime -= diffArgument.getWaterCompensationDeductionTime();
            }
        }
        // 通过关闭时间计算关闭间隔
        // 氮肥关闭间隔
        double nCloseInterval = nCloseTime / fieldUnitList.size();
        // 钾肥关闭间隔
        double kCloseInterval = kCloseTime / fieldUnitList.size();
        // 磷肥关闭间隔
        double pCloseInterval = pCloseTime / fieldUnitList.size();
        // 纯水关闭间隔
        double waterCloseInterval = waterCloseTime / fieldUnitList.size();

        // 组装数据
        JSONObject resultObject = new JSONObject(true);
        // 设置类型
        resultObject.set("type", 1);
        // 设置地块编号
        resultObject.set("fieldID", field.getFieldId());
        // 设置开始时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dataTime", formattedDate);
        // 设置任务编号
        resultObject.set("taskID", task.getTaskId());
        // 设置规划数据
        JSONArray scheduleDataArray = new JSONArray();
        resultObject.set("schedule_Data", scheduleDataArray);
        // 第一个灌溉组
        JSONObject firstUnitJSONObject = new JSONObject(true);
        scheduleDataArray.add(firstUnitJSONObject);
        // 设置组编号
        firstUnitJSONObject.set("groupID", groupManager.getGroupName());
        // 设置阀门
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            firstUnitJSONObject.set("taps", tapsIdList.toString());
        }
        // 水泵编号
        if (pump != null) {
            firstUnitJSONObject.set("pump", pump.getDeviceId());
        }
        // 施肥机编号
        if (fertilizerApplicator != null) {
            firstUnitJSONObject.set("fertilizer_Applicator", fertilizerApplicator.getDeviceId());
        }
        // 设置阶段
        JSONArray schedulingSeqArray = new JSONArray();
        firstUnitJSONObject.set("scheduling_Seq", schedulingSeqArray);
        // 第一阶段
        JSONObject firstStage = new JSONObject(true);
        schedulingSeqArray.add(firstStage);
        firstStage.set("irrigation_Stage", 1);
        firstStage.set("irrigation_Type", 0);
        // 用水量
        Double t1Water = sumSize * headWaterConsumption;
        firstStage.set("irrigation_Water", t1Water);
        // 控制时序
        JSONArray firstControl = new JSONArray();
        firstStage.set("control_Seq", firstControl);
        JSONObject tapsControl = new JSONObject(true);
        firstControl.add(tapsControl);
        tapsControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        tapsControl.set("“control_Time", formattedDate);
        tapsControl.set("control_Cmd", 1);
        // 水泵滞留十秒
        JSONObject pumpControl = new JSONObject(true);
        firstControl.add(pumpControl);
        pumpControl.set("controled_Object_Type", 2);
        if (pump != null) {
            pumpControl.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseTime = baseTime + Math.round(10 * 1000);
        baseTime = pumpBaseTime;
        String pumpTime = sdf.format(pumpBaseTime);
        pumpControl.set("control_Time", pumpTime);
        pumpControl.set("control_Cmd", 1);
        // 非最大的开启和关闭
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
            // 寻找对应的阀门
            Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
            if (waterCompensationNum != maxWaterCompensationNum) {
                // 关闭  （关闭间隔）
                JSONObject waterCompensationControlClose = new JSONObject(true);
                firstControl.add(waterCompensationControlClose);
                waterCompensationControlClose.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlClose.set("controled_Object_ID", device1.getDeviceId());
                }
                long closeBaseTime = baseTime + Math.round(waterCloseInterval * 1000);
                baseTime = closeBaseTime;
                String closeTime = sdf.format(closeBaseTime);
                waterCompensationControlClose.set("control_Time", closeTime);
                waterCompensationControlClose.set("control_Cmd", 0);
                // 打开  （扣除时间）
                JSONObject waterCompensationControlOpen = new JSONObject(true);
                firstControl.add(waterCompensationControlOpen);
                waterCompensationControlOpen.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlOpen.set("controled_Object_ID", device1.getDeviceId());
                }
                long openBaseTime = baseTime + Math.round(waterCompensationDeductionTime * 1000);
                baseTime = openBaseTime;
                String openTime = sdf.format(openBaseTime);
                waterCompensationControlOpen.set("control_Time", openTime);
                waterCompensationControlOpen.set("control_Cmd", 1);
            }
        }
        // 多加一个间隔
        baseTime = baseTime + Math.round(waterCloseInterval * 1000);


        // 第三个阶段
        int count = 1;
        for (Integer sortTypeKey : sortType) {
            JSONObject thirdStage = new JSONObject(true);
            schedulingSeqArray.add(thirdStage);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                thirdStage.set("irrigation_type", 2);
                thirdStage.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                thirdStage.set("irrigation_type", 4);
                thirdStage.set("irrigation_K", number);
            } else {
                number = task.getFertilizerP();
                thirdStage.set("irrigation_type", 3);
                thirdStage.set("irrigation_P", number);
            }
            double T3 = waterAndFertilizer * number / currentSpeed;
            //long t3Timebase = baseTime + Math.round(T3 * 1000);
            //String t3Time = sdf.format(t3Timebase);
            Double t3Water = waterAndFertilizer * number;
            thirdStage.set("irrigation_Water", t3Water);

            // 控制顺序
            JSONArray thirdControl = new JSONArray();
            thirdStage.set("control_Seq", thirdControl);
            // 开启施肥机
            if (count == 1) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                jsonObject.set("control_Time", baseTime);
                jsonObject.set("control_Cmd", 1);
            }
            count++;

            if ("氮".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != maxNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(nCloseInterval * 1000);
                }
            } else if ("钾".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != maxKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(kCloseInterval * 1000);
                }
            } else {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != maxPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(pCloseTime * 1000);
                }
            }

            // 关闭施肥机
            if (count == 4) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("“controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                jsonObject.set("control_Time", baseTime);
                jsonObject.set("control_Cmd", 0);
            }
        }

        // 第四阶段
        JSONObject fourStage = new JSONObject(true);
        schedulingSeqArray.add(fourStage);
        fourStage.set("irrigation_Stage", 4);
        fourStage.set("irrigation_Type", 0);
        // 用水量
        Double t4Water = sumSize * tailWaterConsumption;
        fourStage.set("irrigation_Water", t4Water);
        // 控制时序
        JSONArray fourControl = new JSONArray();
        fourStage.set("control_Seq", fourControl);
        JSONObject pumpCloseObject = new JSONObject(true);
        fourControl.add(pumpCloseObject);
        pumpCloseObject.set("controled_Object_Type", 2);
        if (pump != null){
            pumpCloseObject.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseCloseTime = baseTime + Math.round(3600 * 1000);
        String pumpCloseTime = sdf.format(pumpBaseCloseTime);
        pumpCloseObject.set("control_Time",pumpCloseTime);
        pumpCloseObject.set("control_Cmd", 1);

        JSONObject tapsCloseControl = new JSONObject(true);
        fourControl.add(tapsCloseControl);
        tapsCloseControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        long tapCloseBaseTime = baseTime + Math.round(10 * 1000);
        String tapCloseTime = sdf.format(tapCloseBaseTime);
        tapsCloseControl.set("“control_Time", tapCloseTime);
        tapsCloseControl.set("control_Cmd", 0);
        return JSONUtil.toJsonStr(resultObject);
    }

    public String wordDiffSpeed(List<Device> taps, Device pump, Device fertilizerApplicator,
                           List<Field> fieldUnitList, Argument argument, Task task, Field field,
                           GroupManager groupManager, List<Integer> sortType) {
        // 获取流速
        Double currentSpeed = argument.getCurrent_speed();
        // 水肥比
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        // 头耗水量
        Double headWaterConsumption = argument.getHead_water_consumption();
        // 尾耗水量
        Double tailWaterConsumption = argument.getTail_water_consumption();
        // 耗水量
        Double waterNum = task.getWater();
        // 氮肥
        Double fertilizerN = task.getFertilizerN();
        // 磷肥
        Double fertilizerP = task.getFertilizerP();
        // 钾肥
        Double fertilizerK = task.getFertilizerK();
        // 灌溉单元计算数据对应 map
        Map<Field, DiffArgument> fieldDiffArgumentMap = new HashMap<>();
        // 计算总面积
        double sumSize = 0;
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            sumSize += fieldSize;
            // 初始化 map
            fieldDiffArgumentMap.put(field1, new DiffArgument());
        }
        // 计算单位面积流速
        double sizeSpeed = currentSpeed / sumSize;
        // 头纯水阶段时长
        double headWaterTime = headWaterConsumption / sizeSpeed;
        // 尾纯水阶段时长
        double tailWaterTime = tailWaterConsumption / sizeSpeed;
        // 单位面积最大施氮肥量
        double maxNNum = 0d;
        // 单位面积最大施钾肥量
        double maxKNum = 0d;
        // 单位面积最大施磷肥量
        double maxPNum = 0d;
        // 单位纯水最大补偿量
        double maxWaterCompensationNum = Integer.MIN_VALUE;
        // 灌溉单元计算
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            // 需要钾肥量
            double kNum = fertilizerK / fieldSize;
            // 需要氮肥量
            double nNum = fertilizerN / fieldSize;
            // 需要磷肥量
            double pNum = fertilizerP / fieldSize;
            maxNNum = Math.max(maxNNum, nNum);
            maxKNum = Math.max(maxKNum, kNum);
            maxPNum = Math.max(maxPNum, pNum);
            // 单位纯水补偿量
            double waterCompensationNum = waterNum
                    / ((fieldSize - headWaterConsumption - tailWaterConsumption - kNum - nNum - pNum) * waterAndFertilizer);
            maxWaterCompensationNum = Math.max(maxWaterCompensationNum, waterCompensationNum);
            // 填充map参数
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setKNum(kNum);
            diffArgument.setPNum(pNum);
            diffArgument.setNNum(nNum);
            diffArgument.setWaterCompensationNum(waterCompensationNum);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大施氮肥的时间（即施肥机氮肥模式的时间）
        double maxNTime = (maxNNum * waterAndFertilizer) / sizeSpeed;
        // 最大施钾肥的时间（即施肥机钾肥模式的时间）
        double maxKTime = (maxKNum * waterAndFertilizer) / sizeSpeed;
        // 最大施磷肥的时间（即施肥机磷肥模式的时间）
        double maxPTime = (maxPNum * waterAndFertilizer) / sizeSpeed;
        // 除需肥最大单元外的 扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            // 氮肥
            double nDeductionTime = (maxNTime * (maxNNum - fertilizerN)) / maxNNum;
            // 钾肥
            double kDeductionTime = (maxKTime * (maxKNum - fertilizerN)) / maxKNum;
            // 磷肥
            double pDeductionTime = (maxPTime * (maxPNum - fertilizerN)) / maxPNum;
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setNDeductionTime(nDeductionTime);
            diffArgument.setKDeductionTime(kDeductionTime);
            diffArgument.setPDeductionTime(pDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }

        // 补偿时间
        double compensationTime = maxWaterCompensationNum / currentSpeed;
        // 纯水补偿的扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 获取单元单位纯水补偿量
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            // 纯水补偿的扣除时间
            double waterCompensationDeductionTime = (compensationTime * (maxWaterCompensationNum - waterCompensationNum)) / maxWaterCompensationNum;
            diffArgument.setWaterCompensationDeductionTime(waterCompensationDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 计算关闭时间
        double nCloseTime = maxNTime;
        double pCloseTime = maxPTime;
        double kCloseTime = maxKTime;
        double waterCloseTime = maxWaterCompensationNum;
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 计算氮肥关闭时间
            if (diffArgument.getNNum() != maxNNum) {
                nCloseTime -= diffArgument.getNDeductionTime();
            }
            // 计算磷肥关闭时间
            if (diffArgument.getPNum() != maxPNum) {
                pCloseTime -= diffArgument.getPDeductionTime();
            }
            // 计算钾肥关闭时间
            if (diffArgument.getKNum() != maxKNum) {
                kCloseTime -= diffArgument.getKDeductionTime();
            }
            // 计算纯水关闭时间
            if (diffArgument.getWaterCompensationNum() != maxWaterCompensationNum) {
                waterCloseTime -= diffArgument.getWaterCompensationDeductionTime();
            }
        }
        // 通过关闭时间计算关闭间隔
        // 氮肥关闭间隔
        double nCloseInterval = nCloseTime / fieldUnitList.size();
        // 钾肥关闭间隔
        double kCloseInterval = kCloseTime / fieldUnitList.size();
        // 磷肥关闭间隔
        double pCloseInterval = pCloseTime / fieldUnitList.size();
        // 纯水关闭间隔
        double waterCloseInterval = waterCloseTime / fieldUnitList.size();

        // 组装数据
        JSONObject resultObject = new JSONObject(true);
        // 设置类型
        resultObject.set("type", 1);
        // 设置地块编号
        resultObject.set("fieldID", field.getFieldId());
        // 设置开始时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dataTime", formattedDate);
        // 设置任务编号
        resultObject.set("taskID", task.getTaskId());
        // 设置规划数据
        JSONArray scheduleDataArray = new JSONArray();
        resultObject.set("schedule_Data", scheduleDataArray);
        // 第一个灌溉组
        JSONObject firstUnitJSONObject = new JSONObject(true);
        scheduleDataArray.add(firstUnitJSONObject);
        // 设置组编号
        firstUnitJSONObject.set("groupID", groupManager.getGroupName());
        // 设置阀门
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            firstUnitJSONObject.set("taps", tapsIdList.toString());
        }
        // 水泵编号
        if (pump != null) {
            firstUnitJSONObject.set("pump", pump.getDeviceId());
        }
        // 施肥机编号
        if (fertilizerApplicator != null) {
            firstUnitJSONObject.set("fertilizer_Applicator", fertilizerApplicator.getDeviceId());
        }
        // 设置阶段
        JSONArray schedulingSeqArray = new JSONArray();
        firstUnitJSONObject.set("scheduling_Seq", schedulingSeqArray);
        // 第一阶段
        JSONObject firstStage = new JSONObject(true);
        schedulingSeqArray.add(firstStage);
        firstStage.set("irrigation_Stage", 1);
        firstStage.set("irrigation_Type", 0);
        // 用水量
        Double t1Water = sumSize * headWaterConsumption;
        firstStage.set("irrigation_Water", t1Water);
        // 控制时序
        JSONArray firstControl = new JSONArray();
        firstStage.set("control_Seq", firstControl);
        JSONObject tapsControl = new JSONObject(true);
        firstControl.add(tapsControl);
        tapsControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        tapsControl.set("flow_rate", 0);
        tapsControl.set("control_Cmd", 1);
        // 水泵滞留十秒
        JSONObject pumpControl = new JSONObject(true);
        firstControl.add(pumpControl);
        pumpControl.set("controled_Object_Type", 2);
        if (pump != null) {
            pumpControl.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseTime = baseTime + Math.round(10 * 1000);
        baseTime = pumpBaseTime;
        String pumpTime = sdf.format(pumpBaseTime);
        pumpControl.set("flow_rate", 0);
        pumpControl.set("control_Cmd", 1);
        // 非最大的开启和关闭
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
            // 寻找对应的阀门
            Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
            if (waterCompensationNum != maxWaterCompensationNum) {
                // 关闭  （关闭间隔）
                JSONObject waterCompensationControlClose = new JSONObject(true);
                firstControl.add(waterCompensationControlClose);
                waterCompensationControlClose.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlClose.set("controled_Object_ID", device1.getDeviceId());
                }
                long closeBaseTime = baseTime + Math.round(waterCloseInterval * 1000);
                baseTime = closeBaseTime;
                String closeTime = sdf.format(closeBaseTime);
                waterCompensationControlClose.set("flow_rate", waterCloseInterval * currentSpeed);
                waterCompensationControlClose.set("control_Cmd", 0);
                // 打开  （扣除时间）
                JSONObject waterCompensationControlOpen = new JSONObject(true);
                firstControl.add(waterCompensationControlOpen);
                waterCompensationControlOpen.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlClose.set("controled_Object_ID", device1.getDeviceId());
                }
                long openBaseTime = baseTime + Math.round(waterCompensationDeductionTime * 1000);
                baseTime = openBaseTime;
                String openTime = sdf.format(openBaseTime);
                waterCompensationControlOpen.set("flow_rate", waterCompensationDeductionTime * currentSpeed);
                waterCompensationControlOpen.set("control_Cmd", 1);
            }
        }
        // 多加一个间隔
        baseTime = baseTime + Math.round(waterCloseInterval * 1000);

        // 第三个阶段
        int count = 1;
        for (Integer sortTypeKey : sortType) {
            JSONObject thirdStage = new JSONObject(true);
            schedulingSeqArray.add(thirdStage);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                thirdStage.set("irrigation_type", 2);
                thirdStage.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                thirdStage.set("irrigation_type", 4);
                thirdStage.set("irrigation_K", number);
            } else {
                number = task.getFertilizerP();
                thirdStage.set("irrigation_type", 3);
                thirdStage.set("irrigation_P", number);
            }
            double T3 = waterAndFertilizer * number / currentSpeed;
            Double t3Water = waterAndFertilizer * number;
            thirdStage.set("irrigation_Water", t3Water);

            // 控制顺序
            JSONArray thirdControl = new JSONArray();
            thirdStage.set("control_Seq", thirdControl);
            // 开启施肥机
            if (count == 1) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                jsonObject.set("flow_rate", waterCloseInterval * currentSpeed);
                jsonObject.set("control_Cmd", 1);
            }
            count++;
            double time = 0d;
            if ("氮".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != maxNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }

                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("flow_rate", nCloseInterval * currentSpeed);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("flow_rate", nDeductionTime * currentSpeed);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(nCloseInterval * 1000);
                    time = nCloseInterval;
                }
            } else if ("钾".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != maxKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("flow_rate", kCloseInterval * currentSpeed);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("flow_rate", kDeductionTime * currentSpeed);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(kCloseInterval * 1000);
                    time = kCloseInterval;
                }
            } else {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != maxPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("flow_rate", pCloseInterval * currentSpeed);
                        jsonObjectClose.set("control_Cmd", 0);

                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("flow_rate", pDeductionTime * currentSpeed);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(pCloseTime * 1000);
                    time = pCloseTime;
                }
            }

            // 关闭施肥机
            if (count == 4) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("“controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                jsonObject.set("flow_rate", time * currentSpeed);
                jsonObject.set("control_Cmd", 0);
            }
        }

        // 第四阶段
        JSONObject fourStage = new JSONObject(true);
        schedulingSeqArray.add(fourStage);
        fourStage.set("irrigation_Stage", 4);
        fourStage.set("irrigation_Type", 0);
        // 用水量
        Double t4Water = sumSize * tailWaterConsumption;
        fourStage.set("irrigation_Water", t4Water);
        // 控制时序
        JSONArray fourControl = new JSONArray();
        fourStage.set("control_Seq", fourControl);
        JSONObject pumpCloseObject = new JSONObject(true);
        fourControl.add(pumpCloseObject);
        pumpCloseObject.set("controled_Object_Type", 2);
        if (pump != null){
            pumpCloseObject.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseCloseTime = baseTime + Math.round(3600 * 1000);
        String pumpCloseTime = sdf.format(pumpBaseCloseTime);
        pumpCloseObject.set("flow_rate", 3600 * currentSpeed);
        pumpCloseObject.set("control_Cmd", 1);

        JSONObject tapsCloseControl = new JSONObject(true);
        fourControl.add(tapsCloseControl);
        tapsCloseControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        long tapCloseBaseTime = baseTime + Math.round(10 * 1000);
        String tapCloseTime = sdf.format(tapCloseBaseTime);
        tapsCloseControl.set("flow_rate", 0);
        tapsCloseControl.set("control_Cmd", 0);
        return JSONUtil.toJsonStr(resultObject);
    }



    public String wordDiffMix(List<Device> taps, Device pump, Device fertilizerApplicator,
                           List<Field> fieldUnitList, Argument argument, Task task, Field field,
                           GroupManager groupManager, List<Integer> sortType) {
        // 获取流速
        Double currentSpeed = argument.getCurrent_speed();
        // 水肥比
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        // 头耗水量
        Double headWaterConsumption = argument.getHead_water_consumption();
        // 尾耗水量
        Double tailWaterConsumption = argument.getTail_water_consumption();
        // 耗水量
        Double waterNum = task.getWater();
        // 氮肥
        Double fertilizerN = task.getFertilizerN();
        // 磷肥
        Double fertilizerP = task.getFertilizerP();
        // 钾肥
        Double fertilizerK = task.getFertilizerK();
        // 灌溉单元计算数据对应 map
        Map<Field, DiffArgument> fieldDiffArgumentMap = new HashMap<>();
        // 计算总面积
        double sumSize = 0;
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            sumSize += fieldSize;
            // 初始化 map
            fieldDiffArgumentMap.put(field1, new DiffArgument());
        }
        // 计算单位面积流速
        double sizeSpeed = currentSpeed / sumSize;
        // 头纯水阶段时长
        double headWaterTime = headWaterConsumption / sizeSpeed;
        // 尾纯水阶段时长
        double tailWaterTime = tailWaterConsumption / sizeSpeed;
        // 单位面积最小施氮肥量
        double minNNum = 0d;
        // 单位面积最小施钾肥量
        double minKNum = 0d;
        // 单位面积最小施磷肥量
        double minPNum = 0d;
        // 单位面积最大施氮肥量
        double maxNNum = 0d;
        // 单位面积最大施钾肥量
        double maxKNum = 0d;
        // 单位面积最大施磷肥量
        double maxPNum = 0d;
        // 单位纯水最大补偿量
        double maxWaterCompensationNum = 0d;
        // 灌溉单元计算
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            // 需要钾肥量
            double kNum = fertilizerK / fieldSize;
            // 需要氮肥量
            double nNum = fertilizerN / fieldSize;
            // 需要磷肥量
            double pNum = fertilizerP / fieldSize;
            minNNum = Math.min(minNNum, nNum);
            maxNNum = Math.max(maxNNum, nNum);
            minKNum = Math.min(minKNum, kNum);
            maxKNum = Math.max(maxKNum, kNum);
            minPNum = Math.min(minPNum, pNum);
            maxPNum = Math.max(maxPNum, pNum);
            // 单位纯水补偿量
            double waterCompensationNum = waterNum
                    / ((fieldSize - headWaterConsumption - tailWaterConsumption - kNum - nNum - pNum) * waterAndFertilizer);
            maxWaterCompensationNum = Math.max(maxWaterCompensationNum, waterCompensationNum);
            // 填充map参数
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setKNum(kNum);
            diffArgument.setPNum(pNum);
            diffArgument.setNNum(nNum);
            diffArgument.setWaterCompensationNum(waterCompensationNum);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 氮肥混合肥配比
        double nMixedFertilizerRatio = minNNum * sumSize;
        // 钾肥混合肥配比
        double kMixedFertilizerRatio = minKNum * sumSize;
        // 磷肥混合肥配比
        double pMixedFertilizerRatio = minPNum * sumSize;
        // 混合肥时长
        double mixTime = ((kMixedFertilizerRatio + nMixedFertilizerRatio + pMixedFertilizerRatio) * waterAndFertilizer) / currentSpeed;
        // 单位最大氮肥补偿量
        double nFerCompensationMax = 0d;
        // 单位最大钾肥补偿量
        double kFerCompensationMax = 0d;
        // 单位最大磷肥补偿量
        double pFerCompensationMax = 0d;
        // 单位面积肥补偿量
        for (Field field1 : fieldUnitList) {
            // 单位面积氮肥补偿量
            double nFerCompensation = fertilizerN - minNNum;
            nFerCompensationMax = Math.max(nFerCompensationMax, nFerCompensation);
            // 单位面积钾肥补偿量
            double kFerCompensation = fertilizerK - minKNum;
            kFerCompensationMax = Math.max(kFerCompensation, kFerCompensationMax);
            // 单位面积磷肥补偿量
            double pFerCompensation = fertilizerP - minPNum;
            pFerCompensationMax = Math.max(pFerCompensation, pFerCompensationMax);

            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setNFerCompensation(nFerCompensation);
            diffArgument.setKFerCompensation(kFerCompensation);
            diffArgument.setPFerCompensation(pFerCompensation);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大补偿氮肥时间
        double maxCompensateNTime = (nFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 最大补偿钾肥时间
        double maxCompensateKTime = (kFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 最大补偿磷肥时间
        double maxCompensatePTime = (pFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 计算扣除时间
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double kFerCompensation = diffArgument.getKFerCompensation();
            double nFerCompensation = diffArgument.getNFerCompensation();
            double pFerCompensation = diffArgument.getPFerCompensation();
            // 计算氮肥扣除时间
            double nDeductionTime = (maxCompensateNTime * (nFerCompensationMax - nFerCompensation)) / nFerCompensationMax;
            // 计算钾肥扣除时间
            double kDeductionTime = (maxCompensateKTime * (kFerCompensationMax - kFerCompensation)) / kFerCompensationMax;
            // 计算磷肥扣除时间
            double pDeductionTime = (maxCompensatePTime * (pFerCompensationMax - pFerCompensation)) / pFerCompensationMax;
            diffArgument.setKDeductionTime(kDeductionTime);
            diffArgument.setNDeductionTime(nDeductionTime);
            diffArgument.setPDeductionTime(pDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大施氮肥的时间（即施肥机氮肥模式的时间）
        double maxNTime = (maxNNum * waterAndFertilizer) / sizeSpeed;
        // 最大施钾肥的时间（即施肥机钾肥模式的时间）
        double maxKTime = (maxKNum * waterAndFertilizer) / sizeSpeed;
        // 最大施磷肥的时间（即施肥机磷肥模式的时间）
        double maxPTime = (maxPNum * waterAndFertilizer) / sizeSpeed;

        // 补偿时间
        double compensationTime = maxWaterCompensationNum / currentSpeed;
        // 纯水补偿的扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 获取单元单位纯水补偿量
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            // 纯水补偿的扣除时间
            double waterCompensationDeductionTime = (compensationTime * (maxWaterCompensationNum - waterCompensationNum)) / maxWaterCompensationNum;
            diffArgument.setWaterCompensationDeductionTime(waterCompensationDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 计算关闭时间
        double nCloseTime = maxNTime;
        double pCloseTime = maxPTime;
        double kCloseTime = maxKTime;
        double waterCloseTime = maxWaterCompensationNum;
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 计算氮肥关闭时间
            if (diffArgument.getNNum() != maxNNum) {
                nCloseTime -= diffArgument.getNDeductionTime();
            }
            // 计算磷肥关闭时间
            if (diffArgument.getPNum() != maxPNum) {
                pCloseTime -= diffArgument.getPDeductionTime();
            }
            // 计算钾肥关闭时间
            if (diffArgument.getKNum() != maxKNum) {
                kCloseTime -= diffArgument.getKDeductionTime();
            }
            // 计算纯水关闭时间
            if (diffArgument.getWaterCompensationNum() != maxWaterCompensationNum) {
                waterCloseTime -= diffArgument.getWaterCompensationDeductionTime();
            }
        }
        // 通过关闭时间计算关闭间隔
        // 氮肥关闭间隔
        double nCloseInterval = nCloseTime / fieldUnitList.size();
        // 钾肥关闭间隔
        double kCloseInterval = kCloseTime / fieldUnitList.size();
        // 磷肥关闭间隔
        double pCloseInterval = pCloseTime / fieldUnitList.size();
        // 纯水关闭间隔
        double waterCloseInterval = waterCloseTime / fieldUnitList.size();

        // 组装数据
        JSONObject resultObject = new JSONObject(true);
        // 设置类型
        resultObject.set("type", 1);
        // 设置地块编号
        resultObject.set("fieldID", field.getFieldId());
        // 设置开始时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dataTime", formattedDate);
        // 设置任务编号
        resultObject.set("taskID", task.getTaskId());
        // 设置规划数据
        JSONArray scheduleDataArray = new JSONArray();
        resultObject.set("schedule_Data", scheduleDataArray);
        // 第一个灌溉组
        JSONObject firstUnitJSONObject = new JSONObject(true);
        scheduleDataArray.add(firstUnitJSONObject);
        // 设置组编号
        firstUnitJSONObject.set("groupID", groupManager.getGroupName());
        // 设置阀门
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            firstUnitJSONObject.set("taps", tapsIdList.toString());
        }
        // 水泵编号
        if (pump != null) {
            firstUnitJSONObject.set("pump", pump.getDeviceId());
        }
        // 施肥机编号
        if (fertilizerApplicator != null) {
            firstUnitJSONObject.set("fertilizer_Applicator", fertilizerApplicator.getDeviceId());
        }
        // 设置阶段
        JSONArray schedulingSeqArray = new JSONArray();
        firstUnitJSONObject.set("scheduling_Seq", schedulingSeqArray);
        // 第一阶段
        JSONObject firstStage = new JSONObject(true);
        schedulingSeqArray.add(firstStage);
        firstStage.set("irrigation_Stage", 1);
        firstStage.set("irrigation_Type", 0);
        // 用水量
        Double t1Water = sumSize * headWaterConsumption;
        firstStage.set("irrigation_Water", t1Water);
        // 控制时序
        JSONArray firstControl = new JSONArray();
        firstStage.set("control_Seq", firstControl);
        JSONObject tapsControl = new JSONObject(true);
        firstControl.add(tapsControl);
        tapsControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        tapsControl.set("“control_Time", formattedDate);
        tapsControl.set("control_Cmd", 1);
        // 水泵滞留十秒
        JSONObject pumpControl = new JSONObject(true);
        firstControl.add(pumpControl);
        pumpControl.set("controled_Object_Type", 2);
        if (pump != null) {
            pumpControl.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseTime = baseTime + Math.round(10 * 1000);
        baseTime = pumpBaseTime;
        String pumpTime = sdf.format(pumpBaseTime);
        pumpControl.set("control_Time", pumpTime);
        pumpControl.set("control_Cmd", 1);
        // 非最大的开启和关闭
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
            // 寻找对应的阀门
            Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
            if (waterCompensationNum != maxWaterCompensationNum) {
                // 关闭  （关闭间隔）
                JSONObject waterCompensationControlClose = new JSONObject(true);
                firstControl.add(waterCompensationControlClose);
                waterCompensationControlClose.set("controled_Object_Type", 1);
                if (device1 != null ) {
                    waterCompensationControlClose.set("controled_Object_ID", device1.getDeviceId());
                }
                long closeBaseTime = baseTime + Math.round(waterCloseInterval * 1000);
                baseTime = closeBaseTime;
                String closeTime = sdf.format(closeBaseTime);
                waterCompensationControlClose.set("control_Time", closeTime);
                waterCompensationControlClose.set("control_Cmd", 0);
                // 打开  （扣除时间）
                JSONObject waterCompensationControlOpen = new JSONObject(true);
                firstControl.add(waterCompensationControlOpen);
                waterCompensationControlOpen.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlOpen.set("controled_Object_ID", device1.getDeviceId());
                }
                long openBaseTime = baseTime + Math.round(waterCompensationDeductionTime * 1000);
                baseTime = openBaseTime;
                String openTime = sdf.format(openBaseTime);
                waterCompensationControlOpen.set("control_Time", openTime);
                waterCompensationControlOpen.set("control_Cmd", 1);
            }
        }
        // 多加一个间隔
        baseTime = baseTime + Math.round(waterCloseInterval * 1000);


        // 混合肥
        JSONObject mixJSONObject = new JSONObject(true);
        schedulingSeqArray.add(mixJSONObject);
        mixJSONObject.set("irrigation_Type", 1);
        Double t3Water = waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP);
        mixJSONObject.set("irrigation_Water", t3Water);
        mixJSONObject.set("irrigation_N", fertilizerN);
        mixJSONObject.set("irrigation_P", fertilizerP);
        mixJSONObject.set("irrigation_K", fertilizerK);
        JSONArray mixJSONArray = new JSONArray();
        mixJSONObject.set("control_Seq", mixJSONArray);
        JSONObject mixController = new JSONObject(true);
        mixJSONArray.add(mixController);
        mixController.set("controled_Object_Type", 3);
        if (fertilizerApplicator != null) {
            mixController.set("controled_Object_ID", fertilizerApplicator.getDeviceId());
        }
        String mTime = sdf.format(baseTime);
        mixController.set("control_Time", mTime);
        mixController.set("control_Cmd", 1);

        baseTime = baseTime + Math.round(mixTime * 1000);

        // 第三个阶段
        int count = 1;
        for (Integer sortTypeKey : sortType) {
            JSONObject thirdStage = new JSONObject(true);
            schedulingSeqArray.add(thirdStage);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                thirdStage.set("irrigation_type", 2);
                thirdStage.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                thirdStage.set("irrigation_type", 4);
                thirdStage.set("irrigation_K", number);
            } else {
                number = task.getFertilizerP();
                thirdStage.set("irrigation_type", 3);
                thirdStage.set("irrigation_P", number);
            }
            Double water = waterAndFertilizer * number;
            thirdStage.set("irrigation_Water", water);

            // 控制顺序
            JSONArray thirdControl = new JSONArray();
            thirdStage.set("control_Seq", thirdControl);

            count++;

            if ("氮".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum == minNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }

                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != minNNum && nNum != maxNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != maxNNum && nNum != minNNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);
                    }
                }
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum == minNNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);
                    }
                }
                if (count == 4) {
                    baseTime = baseTime + Math.round(nCloseInterval * 1000);
                }
            } else if ("钾".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum == minKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != minKNum && kNum != maxKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != maxKNum && kNum != minKNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if ( kNum == minKNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                if (count == 4) {
                    baseTime = baseTime + Math.round(kCloseInterval * 1000);
                }
            } else {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum == minPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != minPNum && pNum != maxPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        String closeTime = sdf.format(closeBaseTime);
                        jsonObjectClose.set("control_Time", closeTime);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != maxPNum && pNum != minPNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum == minPNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        jsonObjectOpen.set("control_Time", openTime);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                if (count == 4) {
                    baseTime = baseTime + Math.round(pCloseTime * 1000);
                }
            }

            // 关闭施肥机
            if (count == 4) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("“controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                String format = sdf.format(baseTime);
                jsonObject.set("control_Time", format);
                jsonObject.set("control_Cmd", 0);
            }
        }

        // 第四阶段
        JSONObject fourStage = new JSONObject(true);
        schedulingSeqArray.add(fourStage);
        fourStage.set("irrigation_Stage", 4);
        fourStage.set("irrigation_Type", 0);
        // 用水量
        Double t4Water = sumSize * tailWaterConsumption;
        fourStage.set("irrigation_Water", t4Water);
        // 控制时序
        JSONArray fourControl = new JSONArray();
        fourStage.set("control_Seq", fourControl);
        JSONObject pumpCloseObject = new JSONObject(true);
        fourControl.add(pumpCloseObject);
        pumpCloseObject.set("controled_Object_Type", 2);
        if (pump != null){
            pumpCloseObject.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseCloseTime = baseTime + Math.round(3600 * 1000);
        String pumpCloseTime = sdf.format(pumpBaseCloseTime);
        pumpCloseObject.set("control_Time",pumpCloseTime);
        pumpCloseObject.set("control_Cmd", 1);

        JSONObject tapsCloseControl = new JSONObject(true);
        fourControl.add(tapsCloseControl);
        tapsCloseControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        long tapCloseBaseTime = baseTime + Math.round(10 * 1000);
        String tapCloseTime = sdf.format(tapCloseBaseTime);
        tapsCloseControl.set("“control_Time", tapCloseTime);
        tapsCloseControl.set("control_Cmd", 0);
        return JSONUtil.toJsonStr(resultObject);
    }


    public String wordDiffMixSpeed(List<Device> taps, Device pump, Device fertilizerApplicator,
                              List<Field> fieldUnitList, Argument argument, Task task, Field field,
                              GroupManager groupManager, List<Integer> sortType) {
        // 获取流速
        Double currentSpeed = argument.getCurrent_speed();
        // 水肥比
        Integer waterAndFertilizer = argument.getWater_and_fertilizer();
        // 头耗水量
        Double headWaterConsumption = argument.getHead_water_consumption();
        // 尾耗水量
        Double tailWaterConsumption = argument.getTail_water_consumption();
        // 耗水量
        Double waterNum = task.getWater();
        // 氮肥
        Double fertilizerN = task.getFertilizerN();
        // 磷肥
        Double fertilizerP = task.getFertilizerP();
        // 钾肥
        Double fertilizerK = task.getFertilizerK();
        // 灌溉单元计算数据对应 map
        Map<Field, DiffArgument> fieldDiffArgumentMap = new HashMap<>();
        // 计算总面积
        double sumSize = 0;
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            sumSize += fieldSize;
            // 初始化 map
            fieldDiffArgumentMap.put(field1, new DiffArgument());
        }
        // 计算单位面积流速
        double sizeSpeed = currentSpeed / sumSize;
        // 头纯水阶段时长
        double headWaterTime = headWaterConsumption / sizeSpeed;
        // 尾纯水阶段时长
        double tailWaterTime = tailWaterConsumption / sizeSpeed;
        // 单位面积最小施氮肥量
        double minNNum = 0d;
        // 单位面积最小施钾肥量
        double minKNum = 0d;
        // 单位面积最小施磷肥量
        double minPNum = 0d;
        // 单位面积最大施氮肥量
        double maxNNum = 0d;
        // 单位面积最大施钾肥量
        double maxKNum = 0d;
        // 单位面积最大施磷肥量
        double maxPNum = 0d;
        // 单位纯水最大补偿量
        double maxWaterCompensationNum = Integer.MIN_VALUE;
        // 灌溉单元计算
        for (Field field1 : fieldUnitList) {
            Double fieldSize = field1.getFieldSize();
            // 需要钾肥量
            double kNum = fertilizerK / fieldSize;
            // 需要氮肥量
            double nNum = fertilizerN / fieldSize;
            // 需要磷肥量
            double pNum = fertilizerP / fieldSize;
            minNNum = Math.min(minNNum, nNum);
            maxNNum = Math.max(maxNNum, nNum);
            minKNum = Math.min(minKNum, kNum);
            maxKNum = Math.max(maxKNum, kNum);
            minPNum = Math.min(minPNum, pNum);
            maxPNum = Math.max(maxPNum, pNum);
            // 单位纯水补偿量
            double waterCompensationNum = waterNum
                    / ((fieldSize - headWaterConsumption - tailWaterConsumption - kNum - nNum - pNum) * waterAndFertilizer);
            maxWaterCompensationNum = Math.max(maxWaterCompensationNum, waterCompensationNum);
            // 填充map参数
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setKNum(kNum);
            diffArgument.setPNum(pNum);
            diffArgument.setNNum(nNum);
            diffArgument.setWaterCompensationNum(waterCompensationNum);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 氮肥混合肥配比
        double nMixedFertilizerRatio = minNNum * sumSize;
        // 钾肥混合肥配比
        double kMixedFertilizerRatio = minKNum * sumSize;
        // 磷肥混合肥配比
        double pMixedFertilizerRatio = minPNum * sumSize;
        // 混合肥时长
        double mixTime = ((kMixedFertilizerRatio + nMixedFertilizerRatio + pMixedFertilizerRatio) * waterAndFertilizer) / currentSpeed;
        // 单位最大氮肥补偿量
        double nFerCompensationMax = 0d;
        // 单位最大钾肥补偿量
        double kFerCompensationMax = 0d;
        // 单位最大磷肥补偿量
        double pFerCompensationMax = 0d;
        // 单位面积肥补偿量
        for (Field field1 : fieldUnitList) {
            // 单位面积氮肥补偿量
            double nFerCompensation = fertilizerN - minNNum;
            nFerCompensationMax = Math.max(nFerCompensationMax, nFerCompensation);
            // 单位面积钾肥补偿量
            double kFerCompensation = fertilizerK - minKNum;
            kFerCompensationMax = Math.max(kFerCompensation, kFerCompensationMax);
            // 单位面积磷肥补偿量
            double pFerCompensation = fertilizerP - minPNum;
            pFerCompensationMax = Math.max(pFerCompensation, pFerCompensationMax);

            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            diffArgument.setNFerCompensation(nFerCompensation);
            diffArgument.setKFerCompensation(kFerCompensation);
            diffArgument.setPFerCompensation(pFerCompensation);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大补偿氮肥时间
        double maxCompensateNTime = (nFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 最大补偿钾肥时间
        double maxCompensateKTime = (kFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 最大补偿磷肥时间
        double maxCompensatePTime = (pFerCompensationMax * waterAndFertilizer) / sizeSpeed;
        // 计算扣除时间
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double kFerCompensation = diffArgument.getKFerCompensation();
            double nFerCompensation = diffArgument.getNFerCompensation();
            double pFerCompensation = diffArgument.getPFerCompensation();
            // 计算氮肥扣除时间
            double nDeductionTime = (maxCompensateNTime * (nFerCompensationMax - nFerCompensation)) / nFerCompensationMax;
            // 计算钾肥扣除时间
            double kDeductionTime = (maxCompensateKTime * (kFerCompensationMax - kFerCompensation)) / kFerCompensationMax;
            // 计算磷肥扣除时间
            double pDeductionTime = (maxCompensatePTime * (pFerCompensationMax - pFerCompensation)) / pFerCompensationMax;
            diffArgument.setKDeductionTime(kDeductionTime);
            diffArgument.setNDeductionTime(nDeductionTime);
            diffArgument.setPDeductionTime(pDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 最大施氮肥的时间（即施肥机氮肥模式的时间）
        double maxNTime = (maxNNum * waterAndFertilizer) / sizeSpeed;
        // 最大施钾肥的时间（即施肥机钾肥模式的时间）
        double maxKTime = (maxKNum * waterAndFertilizer) / sizeSpeed;
        // 最大施磷肥的时间（即施肥机磷肥模式的时间）
        double maxPTime = (maxPNum * waterAndFertilizer) / sizeSpeed;

        // 补偿时间
        double compensationTime = maxWaterCompensationNum / currentSpeed;
        // 纯水补偿的扣除时间（每个基本灌溉单元）
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 获取单元单位纯水补偿量
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            // 纯水补偿的扣除时间
            double waterCompensationDeductionTime = (compensationTime * (maxWaterCompensationNum - waterCompensationNum)) / maxWaterCompensationNum;
            diffArgument.setWaterCompensationDeductionTime(waterCompensationDeductionTime);
            fieldDiffArgumentMap.put(field1, diffArgument);
        }
        // 计算关闭时间
        double nCloseTime = maxNTime;
        double pCloseTime = maxPTime;
        double kCloseTime = maxKTime;
        double waterCloseTime = maxWaterCompensationNum;
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            // 计算氮肥关闭时间
            if (diffArgument.getNNum() != maxNNum) {
                nCloseTime -= diffArgument.getNDeductionTime();
            }
            // 计算磷肥关闭时间
            if (diffArgument.getPNum() != maxPNum) {
                pCloseTime -= diffArgument.getPDeductionTime();
            }
            // 计算钾肥关闭时间
            if (diffArgument.getKNum() != maxKNum) {
                kCloseTime -= diffArgument.getKDeductionTime();
            }
            // 计算纯水关闭时间
            if (diffArgument.getWaterCompensationNum() != maxWaterCompensationNum) {
                waterCloseTime -= diffArgument.getWaterCompensationDeductionTime();
            }
        }
        // 通过关闭时间计算关闭间隔
        // 氮肥关闭间隔
        double nCloseInterval = nCloseTime / fieldUnitList.size();
        // 钾肥关闭间隔
        double kCloseInterval = kCloseTime / fieldUnitList.size();
        // 磷肥关闭间隔
        double pCloseInterval = pCloseTime / fieldUnitList.size();
        // 纯水关闭间隔
        double waterCloseInterval = waterCloseTime / fieldUnitList.size();

        // 组装数据
        JSONObject resultObject = new JSONObject(true);
        // 设置类型
        resultObject.set("type", 1);
        // 设置地块编号
        resultObject.set("fieldID", field.getFieldId());
        // 设置开始时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(task.getStartTime());
        long baseTime = task.getStartTime().getTime();
        resultObject.set("dataTime", formattedDate);
        // 设置任务编号
        resultObject.set("taskID", task.getTaskId());
        // 设置规划数据
        JSONArray scheduleDataArray = new JSONArray();
        resultObject.set("schedule_Data", scheduleDataArray);
        // 第一个灌溉组
        JSONObject firstUnitJSONObject = new JSONObject(true);
        scheduleDataArray.add(firstUnitJSONObject);
        // 设置组编号
        firstUnitJSONObject.set("groupID", groupManager.getGroupName());
        // 设置阀门
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            firstUnitJSONObject.set("taps", tapsIdList.toString());
        }
        // 水泵编号
        if (pump != null) {
            firstUnitJSONObject.set("pump", pump.getDeviceId());
        }
        // 施肥机编号
        if (fertilizerApplicator != null) {
            firstUnitJSONObject.set("fertilizer_Applicator", fertilizerApplicator.getDeviceId());
        }
        // 设置阶段
        JSONArray schedulingSeqArray = new JSONArray();
        firstUnitJSONObject.set("scheduling_Seq", schedulingSeqArray);
        // 第一阶段
        JSONObject firstStage = new JSONObject(true);
        schedulingSeqArray.add(firstStage);
        firstStage.set("irrigation_Stage", 1);
        firstStage.set("irrigation_Type", 0);
        // 用水量
        Double t1Water = sumSize * headWaterConsumption;
        firstStage.set("irrigation_Water", t1Water);
        // 控制时序
        JSONArray firstControl = new JSONArray();
        firstStage.set("control_Seq", firstControl);
        JSONObject tapsControl = new JSONObject(true);
        firstControl.add(tapsControl);
        tapsControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        tapsControl.set("flow_rate", 0);
        tapsControl.set("control_Cmd", 1);
        // 水泵滞留十秒
        JSONObject pumpControl = new JSONObject(true);
        firstControl.add(pumpControl);
        pumpControl.set("controled_Object_Type", 2);
        if (pump != null) {
            pumpControl.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseTime = baseTime + Math.round(10 * 1000);
        baseTime = pumpBaseTime;
        String pumpTime = sdf.format(pumpBaseTime);
        pumpControl.set("flow_rate", 0);
        pumpControl.set("control_Cmd", 1);
        // 非最大的开启和关闭
        for (Field field1 : fieldUnitList) {
            DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
            double waterCompensationNum = diffArgument.getWaterCompensationNum();
            double waterCompensationDeductionTime = diffArgument.getWaterCompensationDeductionTime();
            // 寻找对应的阀门
            Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
            if (waterCompensationNum != maxWaterCompensationNum) {
                // 关闭  （关闭间隔）
                JSONObject waterCompensationControlClose = new JSONObject(true);
                firstControl.add(waterCompensationControlClose);
                waterCompensationControlClose.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlClose.set("controled_Object_ID", device1.getDeviceId());
                }
                long closeBaseTime = baseTime + Math.round(waterCloseInterval * 1000);
                double needWater = waterCloseInterval * currentSpeed;
                baseTime = closeBaseTime;
                waterCompensationControlClose.set("flow_rate", needWater);
                waterCompensationControlClose.set("control_Cmd", 0);
                // 打开  （扣除时间）
                JSONObject waterCompensationControlOpen = new JSONObject(true);
                firstControl.add(waterCompensationControlOpen);
                waterCompensationControlOpen.set("controled_Object_Type", 1);
                if (device1 != null) {
                    waterCompensationControlOpen.set("controled_Object_ID", device1.getDeviceId());
                }
                double openNeedWater = waterCompensationDeductionTime * currentSpeed;
                waterCompensationControlOpen.set("flow_rate", openNeedWater);
                waterCompensationControlOpen.set("control_Cmd", 1);
            }
        }
        // 多加一个间隔
        baseTime = baseTime + Math.round(waterCloseInterval * 1000);


        // 混合肥
        JSONObject mixJSONObject = new JSONObject(true);
        schedulingSeqArray.add(mixJSONObject);
        mixJSONObject.set("irrigation_Type", 1);
        Double t3Water = waterAndFertilizer * (fertilizerK + fertilizerN + fertilizerP);
        mixJSONObject.set("irrigation_Water", t3Water);
        mixJSONObject.set("irrigation_N", fertilizerN);
        mixJSONObject.set("irrigation_P", fertilizerP);
        mixJSONObject.set("irrigation_K", fertilizerK);
        JSONArray mixJSONArray = new JSONArray();
        mixJSONObject.set("control_Seq", mixJSONArray);
        JSONObject mixController = new JSONObject(true);
        mixJSONArray.add(mixController);
        mixController.set("controled_Object_Type", 3);
        if (fertilizerApplicator != null) {
            mixController.set("controled_Object_ID", fertilizerApplicator.getDeviceId());
        }
        double mixNeedWater = waterCloseInterval * currentSpeed;
        mixController.set("flow_rate", mixNeedWater);
        mixController.set("control_Cmd", 1);

        baseTime = baseTime + Math.round(mixTime * 1000);

        // 第三个阶段
        int count = 1;
        for (Integer sortTypeKey : sortType) {
            JSONObject thirdStage = new JSONObject(true);
            schedulingSeqArray.add(thirdStage);
            String type = sortTypeMap.get(sortTypeKey);
            // 表示需要的肥料量
            Double number;
            if ("氮".equals(type)) {
                number = task.getFertilizerN();
                thirdStage.set("irrigation_type", 2);
                thirdStage.set("irrigation_N", number);
            } else if ("钾".equals(type)) {
                number = task.getFertilizerK();
                thirdStage.set("irrigation_type", 4);
                thirdStage.set("irrigation_K", number);
            } else {
                number = task.getFertilizerP();
                thirdStage.set("irrigation_type", 3);
                thirdStage.set("irrigation_P", number);
            }
            Double water = waterAndFertilizer * number;
            thirdStage.set("irrigation_Water", water);

            // 控制顺序
            JSONArray thirdControl = new JSONArray();
            thirdStage.set("control_Seq", thirdControl);

            count++;
            double closeTime = 0d;
            if ("氮".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum == minNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = nCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }

                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != minNNum && nNum != maxNNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(nCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = nCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum != maxNNum && nNum != minNNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = nDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);
                    }
                }
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double nDeductionTime = diffArgument.getNDeductionTime();
                    double nNum = diffArgument.getNNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (nNum == minNNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(nDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = nDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);
                    }
                }
                if (count == 4) {
                    closeTime = nCloseInterval;
                }
            } else if ("钾".equals(type)) {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum == minKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = kCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != minKNum && kNum != maxKNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(kCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = kCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (kNum != maxKNum && kNum != minKNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = kDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double kDeductionTime = diffArgument.getKDeductionTime();
                    double kNum = diffArgument.getKNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if ( kNum == minKNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(kDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = kDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                if (count == 4) {
                    baseTime = baseTime + Math.round(kCloseInterval * 1000);
                    closeTime = kCloseInterval;
                }
            } else {
                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum == minPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = pCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != minPNum && pNum != maxPNum) {
                        // 关闭  （关闭间隔）
                        JSONObject jsonObjectClose = new JSONObject(true);
                        thirdControl.add(jsonObjectClose);
                        jsonObjectClose.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectClose.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long closeBaseTime = baseTime + Math.round(pCloseInterval * 1000);
                        baseTime = closeBaseTime;
                        double needWater = pCloseInterval * currentSpeed;
                        jsonObjectClose.set("flow_rate", needWater);
                        jsonObjectClose.set("control_Cmd", 0);
                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum != maxPNum && pNum != minPNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = pDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                for (Field field1 : fieldUnitList) {
                    DiffArgument diffArgument = fieldDiffArgumentMap.get(field1);
                    double pDeductionTime = diffArgument.getPDeductionTime();
                    double pNum = diffArgument.getPNum();
                    // 寻找对应的阀门
                    Device device1 = taps.stream().filter(device -> device.getDeviceManagerNumber().equals(field1.getFieldId())).findFirst().orElse(null);
                    if (pNum == minPNum) {
                        // 开启 （扣除时间）
                        JSONObject jsonObjectOpen = new JSONObject(true);
                        thirdControl.add(jsonObjectOpen);
                        jsonObjectOpen.set("controled_Object_Type", 1);
                        if (device1 != null) {
                            jsonObjectOpen.set("controled_Object_ID", device1.getDeviceId());
                        }
                        long openBaseTime = baseTime + Math.round(pDeductionTime * 1000);
                        baseTime = openBaseTime;
                        String openTime = sdf.format(openBaseTime);
                        double needWater = pDeductionTime * currentSpeed;
                        jsonObjectOpen.set("flow_rate", needWater);
                        jsonObjectOpen.set("control_Cmd", 1);

                    }
                }

                if (count == 4) {
                    closeTime = pCloseTime;
                }
            }

            // 关闭施肥机
            if (count == 4) {
                JSONObject jsonObject = new JSONObject(true);
                thirdControl.add(jsonObject);
                jsonObject.set("controled_Object_Type", 3);
                if (fertilizerApplicator != null) {
                    jsonObject.set("“controled_Object_ID", fertilizerApplicator.getDeviceId());
                }
                String format = sdf.format(baseTime);
                double needWater = closeTime * currentSpeed;
                jsonObject.set("flow_rate", needWater);
                jsonObject.set("control_Cmd", 0);
            }
        }

        // 第四阶段
        JSONObject fourStage = new JSONObject(true);
        schedulingSeqArray.add(fourStage);
        fourStage.set("irrigation_Stage", 4);
        fourStage.set("irrigation_Type", 0);
        // 用水量
        Double t4Water = sumSize * tailWaterConsumption;
        fourStage.set("irrigation_Water", t4Water);
        // 控制时序
        JSONArray fourControl = new JSONArray();
        fourStage.set("control_Seq", fourControl);
        JSONObject pumpCloseObject = new JSONObject(true);
        fourControl.add(pumpCloseObject);
        pumpCloseObject.set("controled_Object_Type", 2);
        if (pump != null){
            pumpCloseObject.set("controled_Object_ID", pump.getDeviceId());
        }
        long pumpBaseCloseTime = baseTime + Math.round(3600 * 1000);
        String pumpCloseTime = sdf.format(pumpBaseCloseTime);
        double needWater = 3600 * currentSpeed;
        pumpCloseObject.set("flow_rate", needWater);
        pumpCloseObject.set("control_Cmd", 1);

        JSONObject tapsCloseControl = new JSONObject(true);
        fourControl.add(tapsCloseControl);
        tapsCloseControl.set("controled_Object_Type", 1);
        if (taps != null) {
            List<String> tapsIdList = taps.stream().map(Device::getDeviceId).collect(Collectors.toList());
            tapsControl.set("controled_Object_ID", tapsIdList.toString());
        }
        long tapCloseBaseTime = baseTime + Math.round(10 * 1000);
        String tapCloseTime = sdf.format(tapCloseBaseTime);
        tapsCloseControl.set("flow_rate", 0);
        tapsCloseControl.set("control_Cmd", 0);
        return JSONUtil.toJsonStr(resultObject);
    }
}
