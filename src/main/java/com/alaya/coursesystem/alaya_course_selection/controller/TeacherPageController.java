// 路径：src/main/java/com/alaya/coursesystem/alaya_course_selection/controller/TeacherPageController.java
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherPageController {

    private final CourseService courseService;

    // 教师课程管理页面
    @GetMapping("/courses")
    public String courseManage(Authentication authentication, Model model) {
        User teacher = (User) authentication.getPrincipal();
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getId());
        model.addAttribute("courses", courses);
        return "teacher/course-manage"; // 指向templates/teacher/course-manage.html
    }

    // 教师创建课程页面（跳转）
    @GetMapping("/courses/create")
    public String createCoursePage() {
        return "teacher/course-create"; // 需新增course-create.html
    }

    // 教师创建课程（提交处理）
    @PostMapping("/courses/create")
    public String createCourse(
            @ModelAttribute Course course, // 接收表单数据
            Authentication authentication,
            Model model) {
        try {
            User teacher = (User) authentication.getPrincipal();
            course.setTeacher(teacher);
            courseService.addCourse(course);
            model.addAttribute("message", "课程创建成功！");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teacher/course-create"; // 失败返回创建页
        }
        return "redirect:/teacher/courses"; // 成功跳转到课程管理页
    }

    // 教师编辑课程页面（跳转）
    @GetMapping("/courses/edit/{id}")
    public String editCoursePage(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "teacher/course-edit"; // 需新增course-edit.html
    }

    // 教师编辑课程（提交处理）
    @PostMapping("/courses/edit")
    public String editCourse(
            @ModelAttribute Course course,
            Authentication authentication,
            Model model) {
        try {
            User teacher = (User) authentication.getPrincipal();
            Course existingCourse = courseService.getCourseById(course.getId());
            if (!existingCourse.getTeacher().getId().equals(teacher.getId())) {
                throw new RuntimeException("无权修改他人课程");
            }
            courseService.addCourse(course);
            model.addAttribute("message", "课程修改成功！");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teacher/course-edit"; // 失败返回编辑页
        }
        return "redirect:/teacher/courses"; // 成功跳转管理页
    }

    // 教师删除课程
    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            User teacher = (User) authentication.getPrincipal();
            Course course = courseService.getCourseById(id);
            if (!course.getTeacher().getId().equals(teacher.getId())) {
                throw new RuntimeException("无权删除他人课程");
            }
            courseService.deleteCourse(id);
            model.addAttribute("message", "课程删除成功！");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/courses";
    }
}