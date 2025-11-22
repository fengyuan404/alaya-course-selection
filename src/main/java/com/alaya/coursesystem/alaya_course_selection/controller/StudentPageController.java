package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentPageController {

    private final CourseService courseService;
    private final CourseSelectionService courseSelectionService;

    // 学生分页查询所有课程（适配PageRequestDTO）
    @GetMapping("/courses")
    public String courseList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        // 手动构建PageRequestDTO，适配Service层参数
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        // 调用修改后的Service方法（返回PageResponseVO）
        PageResponseVO<Course> coursePage = courseService.getAllCoursesByPage(pageRequest);

        // 向前端页面传递分页数据（保持原有model属性名，避免页面报错）
        model.addAttribute("coursePage", coursePage);
        return "student/course-list";
    }

    // 学生搜索课程（适配PageRequestDTO）
    @GetMapping("/courses/search")
    public String searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer credits,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        // 手动构建PageRequestDTO
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        // 调用修改后的Service方法
        PageResponseVO<Course> coursePage = courseService.searchCourses(keyword, credits, pageRequest);

        // 保持原有页面参数传递逻辑
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("credits", credits);
        return "student/course-list";
    }

    // 学生查看课程详情（无需修改）
    @GetMapping("/courses/detail/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "student/course-detail";
    }

    // 选课操作（异常处理适配统一异常类）
    @GetMapping("/selection/select/{courseId}")
    public String selectCourse(@PathVariable Long courseId, Model model) {
        try {
            courseSelectionService.selectCourse(courseId, SecurityContextHolder.getContext().getAuthentication());
            model.addAttribute("message", "选课成功！");
        } catch (UnifiedExceptionHandler.BusinessException e) { // 捕获统一业务异常
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) { // 其他异常兜底
            model.addAttribute("error", "系统错误：" + e.getMessage());
        }
        // 调用courseList时传递默认分页参数
        return courseList(1, 10, model);
    }
}