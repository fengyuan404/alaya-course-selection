package com.alaya.coursesystem.alaya_course_selection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class controller {
    @RestController
    @RequestMapping("/api")
    public static class HelloController {

        @GetMapping("/hello")
        public String hello() {
            return "Hello, Spring Boot";
        }
    }
}
