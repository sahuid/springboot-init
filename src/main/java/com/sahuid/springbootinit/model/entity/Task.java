package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName task
 */
@TableName(value ="task")
@Data
public class Task implements Serializable {
    /**
     * 主键 id
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}