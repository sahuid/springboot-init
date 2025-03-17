package com.sahuid.springbootinit.model.req.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/17 11:03
 **/
@Data
public class UpdateDeviceByIdRequest {

    /**
     * 主键 id
     */
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
}
