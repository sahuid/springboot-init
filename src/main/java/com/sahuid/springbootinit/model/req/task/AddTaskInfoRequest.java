package com.sahuid.springbootinit.model.req.task;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:43
 **/
@Data
public class AddTaskInfoRequest {

    /**
     * 地块的编号
     */
    private String fieldId;

    /**
     * 灌溉单元编号
     */
    private String fieldUnitId;

    /**
     * 灌溉任务编号
     */
    private String taskId;

    /**
     * 需水量（单位：m3）
     */
    private Double water;

    /**
     * 需氮肥量（单位：kg）
     */
    private Double fertilizerN;

    /**
     * 需磷肥量（单位：kg）
     */
    private Double fertilizerP;

    /**
     * 需钾肥量（单位：kg）
     */
    private Double fertilizerK;

    /**
     * 作业的开始时间
     */
    private Date startTime;
}
