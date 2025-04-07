package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.req.group.AddGroupInfoRequest;
import com.sahuid.springbootinit.model.req.group.QueryGroupByPageRequest;
import com.sahuid.springbootinit.model.req.group.UpdateGroupByIdRequest;
import com.sahuid.springbootinit.model.vo.GroupVo;
import com.sahuid.springbootinit.service.GroupManagerService;
import com.sahuid.springbootinit.util.ExcelUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:11
 **/
@RestController
@RequestMapping("/group")
public class GroupController {

    @Resource
    private GroupManagerService groupManagerService;

    @PostMapping("/add")
    public R<Void> addGroupInfo(@RequestBody AddGroupInfoRequest addGroupInfoRequest) {
        groupManagerService.addGroupInfo(addGroupInfoRequest);
        return R.ok("保存成功");
    }


    @GetMapping("/query/page")
    public R<Page<GroupVo>> queryGroupInfoByPage(QueryGroupByPageRequest queryGroupByPageRequest) {
        Page<GroupVo> page = groupManagerService.queryGroupInfoByPage(queryGroupByPageRequest);
        return R.ok(page, "查询成功");
    }


    @PutMapping("/update")
    public R<Void> updateGroupById(@RequestBody UpdateGroupByIdRequest updateGroupByIdRequest) {
        groupManagerService.updateGroupById(updateGroupByIdRequest);
        return R.ok("修改成功");
    }

    @DeleteMapping("/delete")
    public R<Void> deleteGroupById(Long groupId) {
        groupManagerService.deleteGroupById(groupId);
        return R.ok("删除成功");
    }

    @GetMapping("/list")
    public R<List<GroupManager>> queryGroupList() {
        List<GroupManager> list = groupManagerService.queryGroupList();
        return R.ok(list, "查询成功");
    }

    /**
     * 导入分组信息Excel
     */
    @PostMapping("/import")
    public R<Void> importGroup(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.fail(400,"上传文件不能为空");
        }

        // 通过ExcelUtil解析Excel文件
        List<GroupManager> groupList = ExcelUtil.parseExcel(file.getInputStream(), GroupManager.class);

        // 批量保存数据
        boolean result = groupManagerService.saveBatch(groupList);

        if (result) {
            return R.ok("导入成功");
        } else {
            return R.fail(400, "导入失败");
        }
    }

    /**
     * 下载分组导入模板
     */
    @GetMapping("/template/download")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=group_template.xlsx");

        // 创建模板并写入响应流
        ExcelUtil.createTemplate(response.getOutputStream(), GroupManager.class);
    }
}
