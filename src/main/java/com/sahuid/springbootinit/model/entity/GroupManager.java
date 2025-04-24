package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * 
 * @TableName group_manager
 */
@TableName(value ="group_manager")
@Data
public class GroupManager implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 组界限
     */
    private String groupRange;

    /**
     * 组面积大小
     */
    private Double groupSize;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}