package com.sahuid.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName device
 */
@TableName(value ="device")
@Data
public class Device implements Serializable {
    /**
     * 主键 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 设备类型（0：阀门/1：水闸/2：施肥机）
     */
    private Integer deviceType;

    /**
     * 设备灌溉区域的经纬度（json 数组）
     */
    private String deviceAddress;

    /**
     * 设备状态（0表示关；1表示开）
     */
    private Integer deviceStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}