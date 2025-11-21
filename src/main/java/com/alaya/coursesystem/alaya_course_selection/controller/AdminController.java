package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员相关的所有接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // ========== 唯一的成员变量声明 ==========
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final CourseSelectionRepository selectionRepository;

    // ========== 唯一的构造函数（解决重复定义问题） ==========
    public AdminController(
            UserRepository userRepository,
            CourseService courseService,
            CourseSelectionRepository selectionRepository
    ) {
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.selectionRepository = selectionRepository;
    }

    // 1. 管理员查看所有用户
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 2. 管理员查看选课统计
    @GetMapping("/courses/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        List<Course> courses = courseService.getAllCourses();
        Map<String, Object> stats = new HashMap<>();

        for (Course course : courses) {
            // 查询该课程的选课人数
            long selectedCount = selectionRepository.findByCourse(course).size();
            // 组装单门课程的统计数据
            Map<String, Object> courseStats = new HashMap<>();
            courseStats.put("totalCapacity", course.getCapacity());
            courseStats.put("selectedCount", selectedCount);
            courseStats.put("remainingCapacity", course.getCapacity() - selectedCount);
            // 放入总统计（key为课程名）
            stats.put(course.getName(), courseStats);
        }

        return ResponseEntity.ok(stats);
    }
}