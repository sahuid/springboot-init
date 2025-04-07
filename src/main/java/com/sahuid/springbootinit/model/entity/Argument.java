package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName argument
 */
@TableName(value ="argument")
@Data
public class Argument implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 水肥比（a:1）
     */
    private Integer water_and_fertilizer;

    /**
     * 头\尾纯水耗水量h\s 
     */
    private Double water_consumption;

    /**
     * 流速L
     */
    private Double current_speed;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}