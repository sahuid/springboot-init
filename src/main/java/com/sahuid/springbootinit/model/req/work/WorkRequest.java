package com.sahuid.springbootinit.model.req.work;

import lombok.Data;

import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/9 10:02
 **/
@Data
public class WorkRequest {

    private Long fieldId;
    private Long groupId;
    private Long taskId;
    private Long argumentId;
    private List<Integer> sortType;
}
