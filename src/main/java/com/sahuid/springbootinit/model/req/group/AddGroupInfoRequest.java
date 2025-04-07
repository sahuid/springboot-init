package com.sahuid.springbootinit.model.req.group;

import com.sahuid.springbootinit.model.entity.Locational;
import lombok.Data;

import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/17 16:00
 **/
@Data
public class AddGroupInfoRequest {

    private String groupName;

    private List<Locational> groupRange;
}
