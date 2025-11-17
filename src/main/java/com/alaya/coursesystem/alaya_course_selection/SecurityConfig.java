package com.alaya.coursesystem.alaya_course_selection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF（H2控制台不支持CSRF令牌）
                .csrf(csrf -> csrf.disable())
                // 允许H2控制台的所有请求
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // 放行H2控制台
                        .requestMatchers("/hello").permitAll() // 放行测试接口
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                // 允许H2控制台的frame（否则页面会显示空白）
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}