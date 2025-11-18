// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseSelectionService selectionService;

    // 学生选课（直接传递Authentication，无需手动获取User）
    @PostMapping("/select/{courseId}")
    public ResponseEntity<CourseSelection> selectCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        CourseSelection selection = selectionService.selectCourse(courseId, authentication);
        return ResponseEntity.ok(selection);
    }

    // 学生退课
    @DeleteMapping("/drop/{selectionId}")
    public ResponseEntity<Void> dropCourse(
            @PathVariable Long selectionId,
            Authentication authentication) {
        selectionService.dropCourse(selectionId, authentication);
        return ResponseEntity.noContent().build();
    }

    // 查询已选课程
    @GetMapping("/selected")
    public ResponseEntity<List<CourseSelection>> getSelectedCourses(Authentication authentication) {
        return ResponseEntity.ok(selectionService.getSelectedCourses(authentication));
    }
}