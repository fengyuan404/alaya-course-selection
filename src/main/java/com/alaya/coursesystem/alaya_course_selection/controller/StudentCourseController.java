// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.exception.GlobalExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生课程相关控制器（选课/退课/课程查询/搜索）
 * 原接口保留，新增课程列表/搜索/详情接口
 */
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseSelectionService selectionService;
    // 新增：注入课程服务（用于课程查询/搜索）
    private final CourseService courseService;

    // ========== 原有接口（保留，仅优化返回格式） ==========
    // 学生选课
    @PostMapping("/select/{courseId}")
    public ResponseEntity<ApiResponse> selectCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        CourseSelection selection = selectionService.selectCourse(courseId, authentication);
        // 优化：返回统一响应格式
        return ResponseEntity.ok(ApiResponse.success(selection));
    }

    // 学生退课
    @DeleteMapping("/drop/{selectionId}")
    public ResponseEntity<ApiResponse> dropCourse(
            @PathVariable Long selectionId,
            Authentication authentication) {
        selectionService.dropCourse(selectionId, authentication);
        // 优化：返回统一响应格式
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 查询已选课程
    @GetMapping("/selected")
    public ResponseEntity<ApiResponse> getSelectedCourses(Authentication authentication) {
        List<CourseSelection> selectedCourses = selectionService.getSelectedCourses(authentication);
        // 优化：返回统一响应格式
        return ResponseEntity.ok(ApiResponse.success(selectedCourses));
    }

    // ========== 新增：迭代2核心接口（课程查询/搜索/详情） ==========
    // 1. 学生分页查询所有课程（基础列表）
    @GetMapping
    public ResponseEntity<ApiResponse> getCoursesByPage(
            @RequestParam(defaultValue = "1") int pageNum,    // 页码，默认第1页
            @RequestParam(defaultValue = "10") int pageSize) { // 每页条数，默认10条
        Page<Course> coursePage = courseService.getAllCoursesByPage(pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.success(coursePage));
    }

    // 2. 学生模糊搜索课程（支持名称/教师/学分筛选）
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchCourses(
            @RequestParam(required = false) String keyword,  // 搜索关键词（课程名/教师名）
            @RequestParam(required = false) Integer credits, // 学分筛选（可选）
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Course> coursePage = courseService.searchCourses(keyword, credits, pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.success(coursePage));
    }

    // 3. 学生查看单门课程详情
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }
}