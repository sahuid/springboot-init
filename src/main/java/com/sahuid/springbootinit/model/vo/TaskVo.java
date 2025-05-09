package com.sahuid.springbootinit.model.vo;

import com.sahuid.springbootinit.model.entity.Diff;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: mcj
 * @Description: TODO
 * @DateTime: 2025/5/8 16:22
 **/
@Data
public class TaskVo {

    private Long id;

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

    /**
     * 任务类型
     */
    private Integer type;


    List<Diff> diffList;
}
