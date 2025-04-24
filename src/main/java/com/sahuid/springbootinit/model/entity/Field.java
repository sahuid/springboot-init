package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName field
 */
@TableName(value ="field")
@Data
public class Field implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 地块编号
     */
    private String fieldId;

    /**
     * 地块id（用于区分基本灌溉单元）
     */
    private Long fieldParent;

    /**
     * 灌溉单元编号
     */
    private String fieldUnitId;

    /**
     * 经纬度信息
     */
    private String fieldRange;

    /**
     * 灌溉面积
     */
    private Double fieldSize;

    /**
     * 地块名称
     */
    private String fieldName;

    /**
     * 组id
     */
    private Long groupId;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}