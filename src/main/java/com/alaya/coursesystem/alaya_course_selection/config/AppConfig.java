package com.alaya.coursesystem.alaya_course_selection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    // 注册RestTemplate用于HTTP请求
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}