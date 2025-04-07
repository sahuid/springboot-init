package com.sahuid.springbootinit.model.entity;

import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/7 22:01
 **/
@Data
public class Locational {

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;
}
