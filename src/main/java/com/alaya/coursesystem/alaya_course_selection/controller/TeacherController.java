package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.GlobalExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;

    // 教师查询自己创建的课程（适配统一响应格式）
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse> getTeacherCourses(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        // 修复：改为按教师ID查询（适配Course实体的teacher关联）
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 教师创建课程（添加@Valid校验+适配统一响应格式）
    @PostMapping("/courses")
    public ResponseEntity<ApiResponse> createCourse(
            @Valid @RequestBody Course course,  // 添加@Valid触发实体字段校验
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        course.setTeacher(teacher); // 关联教师实体
        Course savedCourse = courseService.addCourse(course);
        return ResponseEntity.ok(ApiResponse.success(savedCourse));
    }

    // 教师修改课程（修复归属权判断+适配统一响应格式）
    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course courseDetails,  // 添加@Valid校验
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        Course existingCourse = courseService.getCourseById(id);

        // 修复：归属权判断（对比teacher实体ID，而非username）
        if (!existingCourse.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("无权修改他人课程");
        }

        // 字段赋值（保留原有逻辑）
        existingCourse.setName(courseDetails.getName());
        existingCourse.setCapacity(courseDetails.getCapacity());
        existingCourse.setCredits(courseDetails.getCredits());
        existingCourse.setDescription(courseDetails.getDescription());
        existingCourse.setSchedule(courseDetails.getSchedule());
        existingCourse.setLocation(courseDetails.getLocation());

        Course updatedCourse = courseService.addCourse(existingCourse);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

    // 教师删除课程（修复归属权判断+删除错误代码+适配统一响应格式）
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse> deleteCourse(
            @PathVariable Long id,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        Course course = courseService.getCourseById(id);

        // 修复：归属权判断（对比teacher实体ID）
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("无权删除他人课程");
        }

        // 移除错误代码：course.setTeacher(teacher); （删除时无需重新关联）
        courseService.deleteCourse(id);
        // 适配统一响应格式（无数据返回，提示删除成功）
        return ResponseEntity.ok(ApiResponse.success("课程删除成功"));
    }
}