package com.sahuid.springbootinit;

import com.sahuid.springbootinit.ws.WebSocketService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.sahuid.springbootinit.mapper")
public class SpringbootInitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringbootInitApplication.class, args);
        // 给 websocket 容器注入 applicationContext
        WebSocketService.setApplicationContext(applicationContext);
    }

}
