package com.sahuid.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sahuid.springbootinit.model.entity.Field;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.AddFieldToGroupRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.vo.FieldVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.List;

/**
* @author wxb
* @description 针对表【field】的数据库操作Service
* @createDate 2025-03-10 00:08:44
*/
public interface FieldService extends IService<Field> {

    /**
     * 添加土地信息
     * @param addFieldInfoRequest
     */
    void addFieldInfo(AddFieldInfoRequest addFieldInfoRequest);

    /**
     * 分页查询土地信息
     * @param queryFieldByPageRequest
     * @return
     */
    Page<FieldVO> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest);

    /**
     * 根据 id 修改地块信息
     * @param updateFieldByIdRequest
     */
    void updateFieldById(UpdateFieldByIdRequest updateFieldByIdRequest);

    /**
     * 根据 id 删除地块信息
     * @param fieldId
     */
    void deleteFieldById(Long fieldId);

    /**
     * 添加土地到分组中
     * @param addFieldToGroupRequest
     */
    void addFieldToGroup(AddFieldToGroupRequest addFieldToGroupRequest);

    /**
     * 查询灌溉单元且没有分组
     * @return
     */
    List<Field> queryFieldNoGroupList();

    /**
     * 查询地块列表
     * @return
     */
    List<Field> queryFieldList();

    /**
     * 查询灌溉单元
     * @return
     */
    List<Field> queryFieldUnitList();

    /**
     * 生成 excel 模板
     * @param outputStream
     * @throws IOException
     */
    void generateTemplate(ServletOutputStream outputStream) throws IOException;

    /**
     * excel 导入
     * @param file
     */
    void importData(MultipartFile file) throws IOException;
}
