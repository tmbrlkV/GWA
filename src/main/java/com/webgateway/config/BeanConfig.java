package com.webgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.socket.sockjs.transport.SockJsServiceConfig;
import org.springframework.web.socket.sockjs.transport.handler.DefaultSockJsService;

@Configuration
@ComponentScan("com.webgateway")
public class BeanConfig {
    @Bean
    public ShaPasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Bean
    public SockJsServiceConfig sockJsServiceConfig() {
        return new DefaultSockJsService(new DefaultManagedTaskScheduler());
    }
}
