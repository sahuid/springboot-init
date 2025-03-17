package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.PageRequest;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.service.FieldService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:11
 **/
@RestController
@RequestMapping("/field")
public class FieldController {

    @Resource
    private FieldService fieldService;

    @PostMapping("/add")
    public R<Void> addFieldInfo(@RequestBody AddFieldInfoRequest addFieldInfoRequest) {
        fieldService.addFieldInfo(addFieldInfoRequest);
        return R.ok("保存成功");
    }


    @GetMapping("/query/page")
    public R<Page<Field>> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest) {
        Page<Field> page = fieldService.queryFieldInfoByPage(queryFieldByPageRequest);
        return R.ok(page, "查询成功");
    }


    @PutMapping("/update")
    public R<Void> updateFieldById(@RequestBody UpdateFieldByIdRequest updateFieldByIdRequest) {
        fieldService.updateFieldById(updateFieldByIdRequest);
        return R.ok("修改成功");
    }

    @DeleteMapping("/delete")
    public R<Void> deleteFieldById(Long fieldId) {
        fieldService.deleteFieldById(fieldId);
        return R.ok("删除成功");
    }
}
