package com.sahuid.springbootinit.model.req.group;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sahuid.springbootinit.model.entity.Locational;
import lombok.Data;

import java.util.List;

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

    /**
     * 组范围
     */
    private List<Locational> groupRange;
}
