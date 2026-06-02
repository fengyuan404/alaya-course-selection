package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.common.Result;
import com.alaya.coursesystem.alaya_course_selection.entity.Grade;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.GradeService;
import com.alaya.coursesystem.alaya_course_selection.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final OperationLogService logService;

    // 教师录入/修改成绩
    @PostMapping
    public ResponseEntity<ApiResponse> saveGrade(
            @RequestParam Long selectionId,
            @RequestParam(required = false) BigDecimal score,
            @RequestParam(required = false) Grade.GradeLevel level,
            @RequestParam(required = false) String comment,
            Authentication authentication) {

        User teacher = (User) authentication.getPrincipal();
        Grade grade = gradeService.saveGrade(selectionId, score, level, comment, teacher);

        // 记录操作日志
        logService.recordLog(teacher.getUsername(), "成绩管理",
                "修改成绩: 选课ID=" + selectionId);

        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    // 学生查询个人成绩（支持按学期筛选）
    @GetMapping("/student")
    public ResponseEntity<ApiResponse> getStudentGrades(
            Authentication authentication,
            @RequestParam(required = false) String semester) {
        User student = (User) authentication.getPrincipal();
        List<Grade> grades;
        if (semester != null && !semester.isEmpty()) {
            grades = gradeService.getStudentGradesBySemester(student.getId(), semester);
        } else {
            grades = gradeService.getStudentGrades(student.getId());
        }

        logService.recordLog(student.getUsername(), "成绩查询",
                semester != null ? "按学期查询成绩: " + semester : "查询个人成绩");
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    // 教师查询课程成绩
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse> getCourseGrades(
            @PathVariable Long courseId,
            Authentication authentication) {

        User teacher = (User) authentication.getPrincipal();
        List<Grade> grades = gradeService.getCourseGrades(courseId, teacher.getId());

        logService.recordLog(teacher.getUsername(), "成绩查询",
                "查询课程成绩: 课程ID=" + courseId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    // 成绩统计分析
    @GetMapping("/course/{courseId}/analysis")
    public ResponseEntity<ApiResponse> analyzeCourseGrades(
            @PathVariable Long courseId,
            Authentication authentication) {

        User teacher = (User) authentication.getPrincipal();
        Map<String, Object> analysis = gradeService.analyzeCourseGrades(courseId, teacher.getId());

        logService.recordLog(teacher.getUsername(), "成绩分析",
                "分析课程成绩: 课程ID=" + courseId);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    @GetMapping("/student/")
    public Result<List<Grade>> getStudentGradesBySemester(
            @RequestParam Long studentId,
            @RequestParam String semester) {
        List<Grade> grades = gradeService.getStudentGradesBySemester(studentId, semester);
        return Result.success(grades);
    }

    /**
     * 教师按学期查询课程成绩
     */
    @GetMapping("/teacher/course/semester")
    public Result<List<Grade>> getCourseGradesBySemester(
            @RequestParam Long courseId,
            @RequestParam Long teacherId,
            @RequestParam String semester) {
        List<Grade> grades = gradeService.getCourseGradesBySemester(courseId, teacherId, semester);
        return Result.success(grades);
    }
}