package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.PageRequest;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Argument;
import com.sahuid.springbootinit.service.ArgumentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/7 22:55
 **/
@RestController
@RequestMapping("/argument")
public class ArgumentController {

    @Resource
    private ArgumentService argumentService;

    @PostMapping("/add")
    public R<Void> addFieldInfo(@RequestBody Argument argument) {
        argumentService.save(argument);
        return R.ok("保存成功");
    }

    @GetMapping("/query/page")
    public R<Page<Argument>> queryArgumentByPage(PageRequest pageRequest) {
        int currentPage = pageRequest.getPage();
        int pageSize = pageRequest.getPageSize();
        Page<Argument> page = new Page<>(currentPage, pageSize);
        argumentService.page(page);
        return R.ok(page);
    }
}
