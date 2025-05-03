package com.sahuid.springbootinit.ws;

import com.sahuid.springbootinit.model.entity.Task;
import com.sahuid.springbootinit.service.TaskService;
import com.sahuid.springbootinit.util.TaskConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/10 1:01
 **/
@ServerEndpoint(value = "/chat")
@Component
@Slf4j
@CrossOrigin(origins = "*")
public class WebSocketService {

    private static ApplicationContext applicationContext;

    private TaskService taskService;

    /**
     * 通过启动类设置 spring 的上下文
     * @param applicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketService.applicationContext = applicationContext;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig sec) {
        log.info("websocket 建立连接成功");
        initSpringBean();
    }

    @OnClose
    public void onClose(Session session) {
        log.info("websocket 链接关闭");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("websocket 接收到了消息， 消息内容是：{}", message);

        try {
            Task tasks = TaskConverter.convertToEntities(message);

            // 批量保存
            taskService.save(tasks);
            session.getBasicRemote().sendText("我接收到了消息，任务已保存");
        } catch (Exception e) {
            log.error("保存失败");
        }


    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("websocket 链接出现问题，错误原因:{}", throwable.getMessage());
    }

    private void initSpringBean() {
        if(taskService != null) {
            return;
        }
        synchronized (WebSocketService.class) {
            if (taskService == null) {
                taskService = applicationContext.getBean(TaskService.class);
            }
        }
    }

}
