package com.sahuid.springbootinit.model.req.field;

import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:14
 **/
@Data
public class AddFieldInfoRequest {


    /**
     * 地块编号
     */
    private String fieldId;

    /**
     * 灌溉单元编号
     */
    private String fieldUnitId;


    /**
     * 地块的位置
     */
    private String fieldRange;

    /**
     * 灌溉面积
     */
    private Double fieldSize;

    /**
     * 地块id（用于区分基本灌溉单元）
     */
    private Long fieldParent;

    /**
     * 地块名称
     */
    private String fieldName;

}
