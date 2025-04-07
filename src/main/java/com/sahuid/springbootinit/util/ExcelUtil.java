package com.sahuid.springbootinit.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    /**
     * 解析Excel文件为对象列表
     */
    public static <T> List<T> parseExcel(InputStream inputStream, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        
        EasyExcel.read(inputStream, clazz, new AnalysisEventListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                result.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 解析完成后的操作
            }
        }).sheet().doRead();
        
        return result;
    }

    /**
     * 创建Excel模板文件
     */
    public static <T> void createTemplate(OutputStream outputStream, Class<T> clazz) {
        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, clazz);
        WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
        writerBuilder.sheet("Sheet1").doWrite(new ArrayList<>());
    }
    
    /**
     * 导出数据到Excel
     */
    public static <T> void exportData(OutputStream outputStream, List<T> data, Class<T> clazz) {
        EasyExcel.write(outputStream, clazz).sheet("Sheet1").doWrite(data);
    }
}