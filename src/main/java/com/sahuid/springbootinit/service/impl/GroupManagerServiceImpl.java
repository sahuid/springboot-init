package com.sahuid.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import com.sahuid.springbootinit.model.entity.Field;
import com.sahuid.springbootinit.model.entity.GroupManager;
import com.sahuid.springbootinit.model.entity.Locational;
import com.sahuid.springbootinit.model.req.group.AddGroupInfoRequest;
import com.sahuid.springbootinit.model.req.group.QueryGroupByPageRequest;
import com.sahuid.springbootinit.model.req.group.UpdateGroupByIdRequest;
import com.sahuid.springbootinit.model.vo.GroupVo;
import com.sahuid.springbootinit.service.FieldGroupService;
import com.sahuid.springbootinit.service.FieldService;
import com.sahuid.springbootinit.service.GroupManagerService;
import com.sahuid.springbootinit.mapper.GroupManagerMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author wxb
 * @description 针对表【group_manager】的数据库操作Service实现
 * @createDate 2025-03-17 15:54:49
 */
@Service
public class GroupManagerServiceImpl extends ServiceImpl<GroupManagerMapper, GroupManager>
        implements GroupManagerService {

    @Resource
    private FieldGroupService fieldGroupService;

    @Resource
    private FieldService fieldService;

    @Override
    public void addGroupInfo(AddGroupInfoRequest addGroupInfoRequest) {
        String groupName = addGroupInfoRequest.getGroupName();
        List<Locational> groupRange = addGroupInfoRequest.getGroupRange();
        if (StrUtil.isBlank(groupName) || groupRange == null) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = new GroupManager();
        groupManager.setGroupName(groupName);
        String jsonStr = JSONUtil.toJsonStr(groupRange);
        groupManager.setGroupRange(jsonStr);
        boolean save = this.save(groupManager);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
    }

    @Override
    public Page<GroupVo> queryGroupInfoByPage(QueryGroupByPageRequest queryGroupByPageRequest) {
        int currPage = queryGroupByPageRequest.getPage();
        int pageSize = queryGroupByPageRequest.getPageSize();
        Page<GroupManager> page = new Page<>(currPage, pageSize);
        this.page(page);
        List<GroupManager> records = page.getRecords();
        List<GroupVo> collect = records.stream()
                .map(this::getGroupVo)
                .collect(Collectors.toList());
        Page<GroupVo> groupVoPage = new Page<>();
        BeanUtil.copyProperties(page, groupVoPage);
        groupVoPage.setRecords(collect);
        return groupVoPage;
    }

    @NotNull
    private GroupVo getGroupVo(GroupManager groupManager) {
        GroupVo groupVo = new GroupVo();
        BeanUtil.copyProperties(groupManager, groupVo, false);
        List<Field> fields = fieldGroupService.queryGroupMappingFieldUnitList(groupManager);
        groupVo.setFieldList(fields);
        StringBuilder stringBuilder = new StringBuilder();
        AtomicReference<Double> size = new AtomicReference<>(0d);
        fields.forEach(field -> {
            String fieldUnitId = field.getFieldUnitId();
            String fieldRange = field.getFieldRange();
            stringBuilder.append("灌溉编号：").append(fieldUnitId).append(":").append(fieldRange);
            Double fieldSize = field.getFieldSize();
            size.updateAndGet(v -> v + fieldSize);
        });
        stringBuilder.append(" ").append("总面积是：").append(size);
        groupVo.setGroupSize(size.get());
        groupVo.setLocationInfo(stringBuilder.toString());
        return groupVo;
    }

    @Override
    public void updateGroupById(UpdateGroupByIdRequest updateGroupByIdRequest) {
        Long userId = updateGroupByIdRequest.getId();
        List<Locational> groupRange = updateGroupByIdRequest.getGroupRange();
        if (userId == null) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = this.getById(userId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("数据不存在");
        }
        BeanUtil.copyProperties(updateGroupByIdRequest, groupManager, false);
        if (groupRange != null) {
            String jsonStr = JSONUtil.toJsonStr(groupManager);
            groupManager.setGroupRange(jsonStr);
        }
        boolean update = this.updateById(groupManager);
        if (!update) {
            throw new RuntimeException("修改失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupById(Long groupId) {
        if (groupId == null) {
            throw new RequestParamException("请求参数错误");
        }
        GroupManager groupManager = this.getById(groupId);
        if (groupManager == null) {
            throw new DataBaseAbsentException("组不存在");
        }
        this.removeById(groupId);
        // 删除 组 和 地块的关联信息
        LambdaUpdateWrapper<Field> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Field::getGroupId, groupId);
        wrapper.set(Field::getGroupId, null);
        fieldService.update(wrapper);
    }

    @Override
    public List<GroupManager> queryGroupList() {
        return this.list();
    }

    @Override
    public void generateTemplate(ServletOutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("分组管理");

            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);  // 组名称
            sheet.setColumnWidth(1, 40 * 256);  // 组界限
            sheet.setColumnWidth(2, 15 * 256);  // 组面积

            // 创建标题行（带样式）
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            String[] headers = {"组名称", "组界限（JSON格式）", "组面积"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("示例分组");

            // JSON示例数据
            String jsonExample = "["
                    + "{\"latitude\":39.9042,\"longitude\":116.4074},"
                    + "{\"latitude\":39.9155,\"longitude\":116.4037},"
                    + "{\"latitude\":39.9088,\"longitude\":116.3973}"
                    + "]";
            exampleRow.createCell(1).setCellValue(jsonExample);
            exampleRow.createCell(2).setCellValue(12.5);

            // 添加数据验证
            addDataValidations(sheet);

            // 添加批注说明
            addCellComment(sheet, 1, 1,
                    "格式要求：\n"
                            + "1. 必须为JSON数组格式\n"
                            + "2. 每个对象必须包含latitude和longitude字段\n"
                            + "3. 经纬度范围：\n"
                            + "   - 纬度：-90 ~ 90\n"
                            + "   - 经度：-180 ~ 180");

            workbook.write(outputStream);
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void processImport(MultipartFile file) throws IOException {
        List<GroupManager> groups = parseExcel(file);
        validateGroups(groups);
        this.saveBatch(groups);
    }

    private List<GroupManager> parseExcel(MultipartFile file) throws IOException {
        List<GroupManager> groups = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 跳过标题行
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                try {
                    GroupManager group = new GroupManager();
                    // 解析各字段
                    group.setGroupName(getCellStringValue(row, 0));
                    group.setGroupRange(parseJsonField(row, 1));
                    group.setGroupSize(parseDouble(row, 2));

                    groups.add(group);
                } catch (Exception e) {
                    errors.add("第" + (row.getRowNum() + 1) + "行数据错误: " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("发现" + errors.size() + "处错误:\n" +
                    String.join("\n", errors));
        }
        return groups;
    }

    private String parseJsonField(Row row, int column) {
        String json = getCellStringValue(row, column);
        try {
            // 验证JSON格式
            JsonNode nodes = objectMapper.readTree(json);
            if (!nodes.isArray()) {
                throw new IllegalArgumentException("必须是JSON数组格式");
            }
            for (JsonNode node : nodes) {
                if (!node.has("latitude") || !node.has("longitude")) {
                    throw new IllegalArgumentException("每个对象必须包含latitude和longitude字段");
                }
                // 验证经纬度范围
                double lat = node.get("latitude").asDouble();
                double lng = node.get("longitude").asDouble();
                if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                    throw new IllegalArgumentException("经纬度范围无效(lat:-90~90, lng:-180~180)");
                }
            }
            return json;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON格式错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }


    private void validateGroups(List<GroupManager> groups) {
        List<String> errors = new ArrayList<>();

        for (GroupManager group : groups) {
            if (StringUtils.isBlank(group.getGroupName())) {
                throw new IllegalArgumentException();
            }
            if (group.getGroupSize() == null || group.getGroupSize() <= 0) {
                throw new IllegalArgumentException();
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    // 辅助方法
    private String getCellStringValue(Row row, int column) {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.getStringCellValue().trim();
    }

    private Double parseDouble(Row row, int column)   {
        Cell cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("数值格式错误");
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

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void addDataValidations(Sheet sheet) {
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();

        // 组面积必须大于0
        DataValidationConstraint numericConstraint = dvHelper.createNumericConstraint(
                DataValidationConstraint.ValidationType.DECIMAL,
                DataValidationConstraint.OperatorType.GREATER_THAN,
                "0", null
        );
        sheet.addValidationData(dvHelper.createValidation(
                numericConstraint,
                new CellRangeAddressList(1, 1000, 2, 2)
        ));
    }

    private void addCellComment(Sheet sheet, int row, int col, String text) {
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 3);
        anchor.setRow2(row + 5);

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        sheet.getRow(row).getCell(col).setCellComment(comment);
    }
}




