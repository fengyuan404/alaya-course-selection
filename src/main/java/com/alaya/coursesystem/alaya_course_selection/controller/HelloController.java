package com.alaya.coursesystem.alaya_course_selection.controller; // 包路径要和启动类一致

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 必须加这个注解，否则Spring不识别为Controller
@RequestMapping("/api") // 类级别路径前缀
public class HelloController {

    @GetMapping("/hello") // 方法级别路径
    public String hello() {
        return "Hello, Spring Boot!";
    }
}