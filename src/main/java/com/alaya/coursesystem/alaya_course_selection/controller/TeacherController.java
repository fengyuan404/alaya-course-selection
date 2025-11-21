package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
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

    // 教师查询自己创建的课程
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getTeacherCourses(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        // 实际应根据教师姓名查询，这里简化返回所有课程
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // 教师创建课程
    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(
            @RequestBody Course course,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        course.setTeacherName(teacher.getUsername()); // 自动设置授课教师为当前登录用户
        return ResponseEntity.ok(courseService.addCourse(course));
    }

    // 教师修改课程
    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course courseDetails,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        Course existingCourse = courseService.getCourseById(id);

        // 验证课程归属权
        if (!existingCourse.getTeacherName().equals(teacher.getUsername())) {
            throw new RuntimeException("无权修改他人课程");
        }

        existingCourse.setName(courseDetails.getName());
        existingCourse.setCapacity(courseDetails.getCapacity());
        existingCourse.setCredits(courseDetails.getCredits());
        existingCourse.setDescription(courseDetails.getDescription());
        existingCourse.setSchedule(courseDetails.getSchedule());
        existingCourse.setLocation(courseDetails.getLocation());

        return ResponseEntity.ok(courseService.addCourse(existingCourse));
    }

    // 教师删除课程
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        Course course = courseService.getCourseById(id);

        if (!course.getTeacherName().equals(teacher.getUsername())) {
            throw new RuntimeException("无权删除他人课程");
        }


        // 实际应先检查是否有学生选课，有则不允许删除
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}