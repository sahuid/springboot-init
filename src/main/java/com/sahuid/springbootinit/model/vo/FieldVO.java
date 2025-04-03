package com.sahuid.springbootinit.model.vo;

import com.sahuid.springbootinit.model.entity.Field;
import lombok.Data;

import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/3 12:27
 **/
@Data
public class FieldVO {

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



    private List<Field> subField;
}
