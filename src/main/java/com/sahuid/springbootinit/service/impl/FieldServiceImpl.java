package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.FieldGroup;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.entity.Locational;
import com.sahuid.springbootinit.model.req.field.AddFieldInfoRequest;
import com.sahuid.springbootinit.model.req.field.AddFieldToGroupRequest;
import com.sahuid.springbootinit.model.req.field.QueryFieldByPageRequest;
import com.sahuid.springbootinit.model.req.field.UpdateFieldByIdRequest;
import com.sahuid.springbootinit.model.vo.FieldVO;
import com.sahuid.springbootinit.service.FieldGroupService;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.mapper.FieldMapper;
import com.sahuid.springbootinit.service.GroupManagerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author wxb
* @description 针对表【field】的数据库操作Service实现
* @createDate 2025-03-10 00:08:44
*/
@Service
public class FieldServiceImpl extends ServiceImpl<FieldMapper, Field>
    implements FieldService{

    @Resource
    @Lazy
    private GroupManagerService groupManagerService;

    @Resource
    @Lazy
    private FieldGroupService fieldGroupService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addFieldInfo(AddFieldInfoRequest addFieldInfoRequest) {
        String fieldId = addFieldInfoRequest.getFieldId();
        List<Locational> fieldRange = addFieldInfoRequest.getFieldRange();
        Double fieldSize = addFieldInfoRequest.getFieldSize();
        if (StringUtils.isAnyBlank(fieldId)) {
            throw new RequestParamException("请求参数缺失");
        }
        if (fieldSize == null || fieldRange == null) {
            throw new RequestParamException("请求参数缺失");
        }
        Field field = new Field();
        BeanUtil.copyProperties(addFieldInfoRequest, field, false);
        String jsonStrRange = JSONUtil.toJsonStr(fieldRange);
        field.setFieldRange(jsonStrRange);
        boolean save = this.save(field);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<FieldVO> queryFieldInfoByPage(QueryFieldByPageRequest queryFieldByPageRequest) {
        int currPage = queryFieldByPageRequest.getPage();
        int pageSize = queryFieldByPageRequest.getPageSize();
        Page<Field> page = new Page<>(currPage, pageSize);
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Field::getFieldParent);
        this.page(page,wrapper);
        // 转化为 vo
        Page<FieldVO> voPage = new Page<>();
        BeanUtil.copyProperties(page, voPage, false);
        // 获取基本灌溉单元
        List<Field> records = page.getRecords();

        List<FieldVO> voList = records.stream().map(field -> {
            Long fieldId = field.getId();
            wrapper.clear();
            wrapper.eq(Field::getFieldParent, fieldId);
            List<Field> fieldList = this.list(wrapper);
            FieldVO fieldVO = new FieldVO();
            BeanUtil.copyProperties(field, fieldVO);
            fieldVO.setSubField(fieldList);
            return fieldVO;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void updateFieldById(UpdateFieldByIdRequest updateFieldByIdRequest) {
        Long fieldId = updateFieldByIdRequest.getId();
        List<Locational> fieldRange = updateFieldByIdRequest.getFieldRange();
        if (fieldId == null) {
            throw new RequestParamException("请求参数错误");
        }
        Field field = this.getById(fieldId);
        if (field == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateFieldByIdRequest, field, false);
        if (fieldRange != null) {
            String jsonStr = JSONUtil.toJsonStr(fieldRange);
            field.setFieldRange(jsonStr);
        }
        boolean update = this.updateById(field);
        if (!update) {
            throw new RuntimeException("数据修改失败");
        }
    }

    @Override
    public void deleteFieldById(Long fieldId) {
        Field field = this.getById(fieldId);
        Long fieldParent = field.getFieldParent();
        if (fieldParent == null) {
            LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Field::getFieldParent, field.getId());
            this.remove(wrapper);
        }
        this.removeById(fieldId);
    }

    @Override
    public void addFieldToGroup(AddFieldToGroupRequest addFieldToGroupRequest) {
        Long groupId = addFieldToGroupRequest.getGroupId();
        List<Long> fieldIds = addFieldToGroupRequest.getFieldId();
        if (groupId == null || fieldIds.isEmpty()) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = groupManagerService.getById(groupId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("组信息为空");
        }
        fieldIds.forEach(fieldId -> {
            Field field = this.getById(fieldId);
            if (field == null) {
                throw new DataBaseAbsentException("土地信息为空");
            }
            field.setGroupId(groupId);
            boolean update = this.updateById(field);
            if (!update) {
                throw new RuntimeException("添加组失败");
            }
        });

    }

    @Override
    public List<Field> queryFieldNoGroupList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Field::getFieldParent);
        wrapper.isNull(Field::getGroupId);
        return this.list(wrapper);
    }

    @Override
    public List<Field> queryFieldList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Field::getFieldParent);
        return this.list(wrapper);
    }

    @Override
    public List<Field> queryFieldUnitList() {
        LambdaQueryWrapper<Field> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Field::getFieldParent);
        return this.list(wrapper);
    }

    @Override
    public void generateTemplate(ServletOutputStream outputStream) throws IOException{
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("地块信息");
            sheet.setDefaultColumnWidth(20);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"地块编号", "经纬度信息", "灌溉面积", "地块名称"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("DK-2023001");

            // 设置JSON示例（带格式提醒）
            String jsonExample = "["
                    + "{\"latitude\":39.9042,\"longitude\":116.4074},"
                    + "{\"latitude\":31.2304,\"longitude\":121.4737}"
                    + "]";
            exampleRow.createCell(1).setCellValue(jsonExample);

            exampleRow.createCell(2).setCellValue(50.5);
            exampleRow.createCell(3).setCellValue("示例地块");

            // 添加批注说明
            addCellComment(sheet, 1, 1,
                    "请按以下格式填写：\n"
                            + "1. 必须为JSON数组格式\n"
                            + "2. 每个对象包含latitude和longitude字段\n"
                            + "3. 经纬度使用WGS84坐标系");

            workbook.write(outputStream);
        }
    }

    @Override
    public void importData(MultipartFile file) throws IOException{
        List<Field> entities = parseExcel(file);
        validateEntities(entities);
        this.saveBatch(entities);
    }

    private List<Field> parseExcel(MultipartFile file) throws IOException {
        List<Field> entities = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 读取标题行
            Row headerRow = rows.next();
            Map<String, Integer> headers = parseHeaders(headerRow);

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                Field entity = new Field();

                // 解析各字段
                entity.setFieldId(getCellStringValue(row, headers.get("地块编号")));
                entity.setFieldRange(getCellStringValue(row, headers.get("经纬度信息")));
                entity.setFieldSize(parseDouble(row, headers.get("灌溉面积")));
                entity.setFieldName(getCellStringValue(row, headers.get("地块名称")));

                entities.add(entity);
            }
        }
        return entities;
    }

    private Map<String, Integer> parseHeaders(Row headerRow) {
        Map<String, Integer> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim();
            headers.put(header, cell.getColumnIndex());
        }
        return headers;
    }

    private String getCellStringValue(Row row, int column) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.getStringCellValue().trim();
    }

    private Double parseDouble(Row row, int column) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            String value = cell.getStringCellValue();
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("第" + (row.getRowNum()+1) + "行灌溉面积格式错误");
            }
        }
    }

    private void validateEntities(List<Field> entities) {
        for (Field entity : entities) {
            // 校验JSON格式
            try {
                JsonNode nodes = objectMapper.readTree(entity.getFieldRange());
                if (!nodes.isArray()) {
                    throw new IllegalArgumentException("经纬度信息必须是JSON数组");
                }
                for (JsonNode node : nodes) {
                    if (!node.has("latitude") || !node.has("longitude")) {
                        throw new IllegalArgumentException("缺少latitude/longitude字段");
                    }
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("JSON格式错误");
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    // 添加单元格批注（辅助方法）
    private void addCellComment(Sheet sheet, int rowNum, int colNum, String text) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(colNum);
        anchor.setRow1(rowNum);
        anchor.setCol2(colNum + 2);
        anchor.setRow2(rowNum + 4);

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        Cell cell = sheet.getRow(rowNum).getCell(colNum);
        cell.setCellComment(comment);
    }
}




