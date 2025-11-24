// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生课程相关控制器（选课/退课/课程查询/搜索）
 */
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseSelectionService selectionService;
    private final CourseService courseService;

    // ========== 原有接口（保持不变） ==========
    // 学生选课
    @PostMapping("/select/{courseId}")
    public ResponseEntity<ApiResponse> selectCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        CourseSelection selection = selectionService.selectCourse(courseId, authentication);
        return ResponseEntity.ok(ApiResponse.success(selection));
    }

    // 学生退课
    @DeleteMapping("/drop/{selectionId}")
    public ResponseEntity<ApiResponse> dropCourse(
            @PathVariable Long selectionId,
            Authentication authentication) {
        selectionService.withdrawCourse(selectionId, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 查询已选课程
    @GetMapping("/selected")
    public ResponseEntity<ApiResponse> getSelectedCourses(Authentication authentication) {
        List<CourseSelection> selectedCourses = selectionService.getMySchedule(authentication);
        return ResponseEntity.ok(ApiResponse.success(selectedCourses));
    }

    // 学生查看单门课程详情（保持不变）
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    // ========== 分页接口修改（适配Service层） ==========
    // 1. 学生分页查询所有课程（基础列表）
    @GetMapping
    public ResponseEntity<UnifiedExceptionHandler.ApiResponse> getCoursesByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,  // 单独接收分页参数
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,     // 单独接收搜索参数
            @RequestParam(required = false) Integer credits     // Integer允许空，避免转换失败
    ) {
        // 手动构建PageRequestDTO（避免绑定失败）
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        pageRequest.setKeyword(keyword);
        pageRequest.setCredits(credits);

        // 2. 正常业务逻辑


        PageResponseVO<Course> coursePage = courseService.getAllCoursesByPage(pageRequest);
        return ResponseEntity.ok(UnifiedExceptionHandler.ApiResponse.success(coursePage));
    }

    // 2. 学生模糊搜索课程（支持名称/教师/学分筛选）
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchCourses(
            @RequestParam(required = false) String keyword,  // 搜索关键词（课程名/教师名）
            @RequestParam(required = false) Integer credits, // 学分筛选（可选）
            // 分页参数改为PageRequestDTO
            @Valid PageRequestDTO pageRequest) {
        PageResponseVO<Course> coursePage = courseService.searchCourses(keyword, credits, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(coursePage));
    }
}