package com.sahuid.springbootinit.model.req.field;

import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:14
 **/
@Data
public class AddFieldToGroupRequest {


    /**
     * 地块id
     */
    private Long fieldId;

    /**
     * 分组id
     */
    private Long groupId;

}
