package com.sahuid.springbootinit.model.req.field;

import lombok.Data;

import java.util.List;

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
    private List<Long> fieldId;

    /**
     * 分组id
     */
    private Long groupId;

}
