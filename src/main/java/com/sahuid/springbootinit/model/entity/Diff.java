package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName diff
 */
@TableName(value ="diff")
@Data
public class Diff implements Serializable {
    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 灌溉单元编号
     */
    private String fieldUnitId;

    /**
     * 需水量
     */
    private Double water;

    /**
     * 需氮量
     */
    private Double fertilizerN;

    /**
     * 需磷量
     */
    private Double fertilizerP;

    /**
     * 需钾量
     */
    private Double fertilizerK;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}