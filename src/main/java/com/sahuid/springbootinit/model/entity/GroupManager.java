package com.sahuid.springbootinit.model.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.sahuid.springbootinit.config.JsonConverter;
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
    @ExcelProperty("ID")
    private Long id;

    /**
     * 组名称
     */
    @ExcelProperty("分组名称")
    private String groupName;

    /**
     * 组界限
     */
    @ExcelProperty(value = "位置信息", converter = JsonConverter.class)
    private String groupRange;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}