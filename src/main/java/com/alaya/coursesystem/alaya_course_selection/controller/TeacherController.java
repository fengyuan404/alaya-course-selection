// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;

    // 实现/api/teacher/courses接口（教师查询课程）
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getTeacherCourses() {
        // 测试阶段返回空列表即可（核心是让接口存在）
        return ResponseEntity.ok(List.of());
    }
}