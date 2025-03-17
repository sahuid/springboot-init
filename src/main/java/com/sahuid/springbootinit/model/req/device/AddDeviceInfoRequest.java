package com.sahuid.springbootinit.model.req.device;

import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:32
 **/
@Data
public class AddDeviceInfoRequest {

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 设备类型（0：阀门/1：水闸/2：施肥机）
     */
    private Integer deviceType;


    /**
     * 设备状态（0表示关；1表示开）
     */
    private Integer deviceStatus;

    /**
     * 设备位置信息
     */
    private String deviceAddress;
}
