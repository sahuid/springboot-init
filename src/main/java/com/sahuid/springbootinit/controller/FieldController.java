package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.AddFieldToGroupRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.vo.FieldVO;
import com.sahuid.springbootinit.service.FieldService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public R<Page<FieldVO>> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest) {
        Page<FieldVO> page = fieldService.queryFieldInfoByPage(queryFieldByPageRequest);
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

    /**
     * 添加基本灌溉单元到组中
     * @param addFieldToGroupRequest
     * @return
     */
    @PostMapping("/to/group")
    public R<Void> addFieldToGroup(@RequestBody AddFieldToGroupRequest addFieldToGroupRequest){
        fieldService.addFieldToGroup(addFieldToGroupRequest);
        return R.ok("添加成功");
    }

    /**
     * 查找所有没有设置分组的灌溉单元
     * @return
     */
    @GetMapping("/list")
    public R<List<Field>> queryFieldNoGroupList() {
        List<Field> list = fieldService.queryFieldNoGroupList();
        return R.ok(list, "查询成功");
    }

    @GetMapping("/list/unit")
    public R<List<Field>> queryFieldUnitList() {
        List<Field> list = fieldService.queryFieldUnitList();
        return R.ok(list, "查询成功");
    }

    @GetMapping("/list/field")
    public R<List<Field>> queryFieldList() {
        List<Field> list = fieldService.queryFieldList();
        return R.ok(list, "查询成功");
    }
}
