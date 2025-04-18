package com.sahuid.springbootinit.model.req.task;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:43
 **/
@Data
public class TaskFieldUnitsToGroupRequest {

    /**
     * 组id
     */
    private Long groupId;

    /**
     * 任务id
     */
    private Long taskId;
}
