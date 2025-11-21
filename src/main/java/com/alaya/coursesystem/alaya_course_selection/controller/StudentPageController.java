package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.exception.GlobalExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService; // 导入选课Service
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder; // 导入SecurityContextHolder
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentPageController {

    private final CourseService courseService;
    private final CourseSelectionService courseSelectionService; // 注入选课Service

    // 学生分页查询所有课程
    @GetMapping("/courses")
    public String courseList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        Page<Course> coursePage = courseService.getAllCoursesByPage(pageNum, pageSize);
        model.addAttribute("coursePage", coursePage);
        return "student/course-list";
    }

    // 学生搜索课程（支持名称/教师/学分）
    @GetMapping("/courses/search")
    public String searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer credits,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        Page<Course> coursePage = courseService.searchCourses(keyword, credits, pageNum, pageSize);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("credits", credits);
        return "student/course-list";
    }

    // 学生查看课程详情
    @GetMapping("/courses/detail/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "student/course-detail";
    }

    // 选课操作（跳转+处理）
    @GetMapping("/selection/select/{courseId}")
    public String selectCourse(@PathVariable Long courseId, Model model) {
        try {
            courseSelectionService.selectCourse(courseId, SecurityContextHolder.getContext().getAuthentication());
            model.addAttribute("message", "选课成功！");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return courseList(1, 10, model);
    }
}