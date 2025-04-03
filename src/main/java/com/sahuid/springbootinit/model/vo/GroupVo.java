package com.sahuid.springbootinit.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sahuid.springbootinit.model.entity.Field;
import lombok.Data;

import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/17 16:37
 **/
@Data
public class GroupVo {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 关联的地块信息
     */
    private List<Field> fieldList;

    /**
     * 位置信息
     */
    private String locationInfo;
}
