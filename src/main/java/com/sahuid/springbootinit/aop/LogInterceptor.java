package com.sahuid.springbootinit.aop;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class LogInterceptor {

    @Around("execution(* com.sahuid.springbootinit.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 生成唯一的id
        String requestId = UUID.randomUUID().toString();
        String uri = request.getRequestURI();
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        String reqParam = "[" + StrUtil.join(",", args) + "]";
        // 输出请求日志
        log.info("request start, id: {}, path: {}, ip: {}, params: {}",
                requestId, uri, request.getRemoteHost(), reqParam);
        // 执行原方法
        Object result = joinPoint.proceed();
        // 响应日志
        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        log.info("request end, id: {}, cast: {} ms", requestId, totalTime);
        return result;
    }
}
