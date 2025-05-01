package com.sahuid.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.common.R;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.req.group.AddGroupInfoRequest;
import com.sahuid.springbootinit.model.req.group.QueryGroupByPageRequest;
import com.sahuid.springbootinit.model.req.group.UpdateGroupByIdRequest;
import com.sahuid.springbootinit.model.vo.GroupVo;
import com.sahuid.springbootinit.service.GroupManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 0:11
 **/
@RestController
@RequestMapping("/group")
@Slf4j
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

    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("分组管理模板.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            groupManagerService.generateTemplate(response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("模板下载失败", e);
        }
    }

    @PostMapping("/import")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            groupManagerService.processImport(file);
            return R.ok();
        } catch (IOException e) {
            return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件读取失败: " + e.getMessage());
        }
    }
}
