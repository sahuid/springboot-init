package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName field_group
 */
@TableName(value ="field_group")
@Data
public class FieldGroup implements Serializable {
    /**
     * 地块id
     */
    private Long fieldId;

    /**
     * 分组id
     */
    private Long groupId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}