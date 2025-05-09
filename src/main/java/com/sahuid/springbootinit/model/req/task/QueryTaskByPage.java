package com.sahuid.springbootinit.model.req.task;

import com.sahuid.springbootinit.common.PageRequest;
import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 21:41
 **/
@Data
public class QueryTaskByPage extends PageRequest {
    private String keyword;

    private String taskId;

    private String startTime;
}
