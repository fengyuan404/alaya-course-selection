package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.service.GradeService;
import com.alaya.coursesystem.alaya_course_selection.dto.GradeBatchDTO;
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
    private final CourseSelectionService courseSelectionService;
    private final GradeService gradeService;

    // 教师查询自己创建的课程（适配统一响应格式）
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse> getTeacherCourses(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        // 修复：改为按教师ID查询（适配Course实体的teacher关联）
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 教师获取课程详情（编辑用）
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
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

    // 教师获取课程选课学生列表（分页）
    @GetMapping("/courses/students")
    public ResponseEntity<ApiResponse> getCourseStudents(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        PageResponseVO<CourseSelection> result = courseSelectionService.getCourseStudentsPage(courseId, keyword, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 教师批量保存成绩
    @PostMapping("/grades/batch")
    public ResponseEntity<ApiResponse> batchSaveGrades(
            @RequestBody List<GradeBatchDTO> gradeList,
            Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        gradeService.batchSaveGrades(gradeList, teacher);
        return ResponseEntity.ok(ApiResponse.success("批量保存成功"));
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