package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.Device;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.device.AddDeviceInfoRequest;
import com.sahuid.springbootinit.model.req.device.QueryDeviceByPageRequest;
import com.sahuid.springbootinit.model.req.device.UpdateDeviceByIdRequest;

/**
* @author wxb
* @description 针对表【device】的数据库操作Service
* @createDate 2025-03-10 00:08:44
*/
public interface DeviceService extends IService<Device> {

    /**
     * 保存设备信息
     * @param addDeviceInfoRequest
     */
    void addDeviceInfo(AddDeviceInfoRequest addDeviceInfoRequest);

    /**
     * 分页查询分页信息
     * @param queryDeviceByPageRequest
     * @return
     */
    Page<Device> queryDeviceInfoByPage(QueryDeviceByPageRequest queryDeviceByPageRequest);

    /**
     * 根据 id 修改设备信息
     * @param updateDeviceByIdRequest
     */
    void updateDeviceById(UpdateDeviceByIdRequest updateDeviceByIdRequest);

    /**
     * 根据 id 删除设备信息
     * @param deviceId
     */
    void deleteDeviceById(Long deviceId);
}
