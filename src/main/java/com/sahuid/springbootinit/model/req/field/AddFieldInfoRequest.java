package com.sahuid.springbootinit.model.req.field;

import com.sahuid.springbootinit.model.entity.Locational;
import lombok.Data;

import java.util.List;

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
    private List<Locational> fieldRange;

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
