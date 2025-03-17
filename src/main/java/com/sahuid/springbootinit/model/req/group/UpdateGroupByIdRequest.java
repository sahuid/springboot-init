package com.sahuid.springbootinit.model.req.group;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/17 16:02
 **/
@Data
public class UpdateGroupByIdRequest {


    /**
     * 主键id
     */
    private Long id;

    /**
     * 组名称
     */
    private String groupName;
}
