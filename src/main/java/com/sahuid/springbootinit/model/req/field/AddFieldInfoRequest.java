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

}
