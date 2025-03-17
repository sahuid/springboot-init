package com.sahuid.springbootinit.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

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

    @OnOpen
    public void onOpen(Session session, EndpointConfig sec) {
        log.info("websocket 建立连接成功");
    }

    @OnClose
    public void onClose(Session session) {
        log.info("websocket 链接关闭");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("websocket 接收到了消息， 消息内容是：{}", message);
        try {
            session.getBasicRemote().sendText("我接收到了消息，消息内容是" + message);
        } catch (IOException e) {
            log.error("WebSocket 发送消息失败: {}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("websocket 链接出现问题，错误原因:{}", throwable.getMessage());
    }

}
