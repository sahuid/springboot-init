package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Device;
import com.sahuid.springbootinit.model.req.device.AddDeviceInfoRequest;
import com.sahuid.springbootinit.model.req.device.QueryDeviceByPageRequest;
import com.sahuid.springbootinit.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:31
 **/
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping("/add")
    public R<Void> addDeviceInfo(@RequestBody AddDeviceInfoRequest addDeviceInfoRequest) {
        deviceService.addDeviceInfo(addDeviceInfoRequest);
        return R.ok("保存成功");
    }


    @GetMapping("/query/page")
    public R<Page<Device>> queryDeviceInfoByPage(QueryDeviceByPageRequest queryDeviceByPageRequest) {
        Page<Device> page = deviceService.queryDeviceInfoByPage(queryDeviceByPageRequest);
        return R.ok(page, "查询成功");
    }
}
