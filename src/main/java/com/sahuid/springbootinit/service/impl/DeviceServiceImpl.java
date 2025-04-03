package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Device;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.req.device.AddDeviceInfoRequest;
import com.sahuid.springbootinit.model.req.device.QueryDeviceByPageRequest;
import com.sahuid.springbootinit.model.req.device.UpdateDeviceByIdRequest;
import com.sahuid.springbootinit.service.DeviceService;
import com.sahuid.springbootinit.mapper.DeviceMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author wxb
* @description 针对表【device】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device>
    implements DeviceService{

    @Override
    public void addDeviceInfo(AddDeviceInfoRequest addDeviceInfoRequest) {
        String deviceId = addDeviceInfoRequest.getDeviceId();
        Integer deviceType = addDeviceInfoRequest.getDeviceType();
        String deviceManagerNumber = addDeviceInfoRequest.getDeviceManagerNumber();

        if (StringUtils.isAnyBlank(deviceManagerNumber, deviceId)) {
            throw new RequestParamException("请求参数缺失");
        }

        if (deviceType == null) {
            throw new RequestParamException("请求参数缺失");
        }

        Device device = new Device();
        BeanUtil.copyProperties(addDeviceInfoRequest, device, false);
        boolean save = this.save(device);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<Device> queryDeviceInfoByPage(QueryDeviceByPageRequest queryDeviceByPageRequest) {
        int currPage = queryDeviceByPageRequest.getPage();
        int pageSize = queryDeviceByPageRequest.getPageSize();
        Page<Device> page = new Page<>(currPage, pageSize);
        this.page(page);
        return page;
    }

    @Override
    public void updateDeviceById(UpdateDeviceByIdRequest updateDeviceByIdRequest) {
        Long deviceId = updateDeviceByIdRequest.getId();
        if (deviceId == null) {
            throw new RequestParamException("请求参数错误");
        }
        Device device = this.getById(deviceId);
        if (device == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateDeviceByIdRequest, device, false);
        boolean update = this.updateById(device);
        if (!update) {
            throw new RuntimeException("数据修改失败");
        }
    }

    @Override
    public void deleteDeviceById(Long deviceId) {
        if (deviceId == null) {
            throw new RequestParamException("请求参数缺失");
        }
        boolean remove = this.removeById(deviceId);
        if (!remove){
            throw new RuntimeException("删除失败");
        }
    }
}




