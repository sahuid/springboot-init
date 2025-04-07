package com.sahuid.springbootinit.config;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON转换器，用于Excel中的复杂对象转换
 */
public class JsonConverter implements Converter<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Class supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws IOException {
        String value = cellData.getStringValue();
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        Class<?> clazz = contentProperty.getField().getType();
        return OBJECT_MAPPER.readValue(value, clazz);
    }

    @Override
    public WriteCellData<?> convertToExcelData(Object value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws JsonProcessingException {
        if (value == null) {
            return new WriteCellData<>("");
        }
        
        String jsonValue = OBJECT_MAPPER.writeValueAsString(value);
        return new WriteCellData<>(jsonValue);
    }
}