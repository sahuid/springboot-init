package com.sahuid.springbootinit.common;

import com.sahuid.springbootinit.exception.DataBaseAbsentException;
import com.sahuid.springbootinit.exception.RequestParamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RequestParamException.class)
    public R<?> requestParamExceptionHandler(RequestParamException e) {
        log.error("RequestParamException:请求参数错误");
        return R.fail(404, "请求参数错误");
    }

    @ExceptionHandler(DataBaseAbsentException.class)
    public R<?> dataBaseAbsentException(DataBaseAbsentException e) {
        log.error("DataBaseAbsentException:数据库不存在错误");
        return R.ok("", "数据不存在");
    }
}
