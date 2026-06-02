package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.service.CourseService;
import com.alaya.coursesystem.alaya_course_selection.service.UserService;
import com.alaya.coursesystem.alaya_course_selection.vo.CourseStatVO;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final CourseSelectionRepository selectionRepository;

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Page<User> page = userService.getUsersPage(keyword, role, pageNum, pageSize);
        page.getContent().forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success(PageResponseVO.from(page)));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        created.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        updated.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id, Authentication auth) {
        User currentUser = (User) auth.getPrincipal();
        if (currentUser.getId().equals(id)) {
            throw new RuntimeException("不能删除自己");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
    }

    @PostMapping("/users/{id}/reset-pwd")
    public ResponseEntity<ApiResponse> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success("密码重置成功，默认密码：123456"));
    }

    // ==================== 教师列表 ====================

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse> getTeachers() {
        List<User> teachers = userService.getTeachers();
        teachers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    // ==================== 课程统计 ====================

    @GetMapping("/courses/stats")
    public ResponseEntity<ApiResponse> getCourseStats(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long teacherId) {

        List<Course> allCourses = courseService.getAllCourses();

        // 筛选：学期用 contains 模糊匹配，兼容不同格式
        List<Course> filtered = new ArrayList<>();
        for (Course c : allCourses) {
            if (semester != null && !semester.isEmpty()
                    && (c.getSemester() == null || !c.getSemester().contains(semester))) {
                continue;
            }
            if (teacherId != null && (c.getTeacher() == null || !teacherId.equals(c.getTeacher().getId()))) {
                continue;
            }
            filtered.add(c);
        }

        // 构建统计项
        List<CourseStatVO.CourseStatItem> items = new ArrayList<>();
        long totalSelected = 0;
        long hotCourseCount = 0;

        for (Course c : filtered) {
            long selectedCount = selectionRepository.countByCourseAndStatus(c, "SELECTED");
            CourseStatVO.CourseStatItem item = new CourseStatVO.CourseStatItem();
            item.setName(c.getName());
            item.setTeacher(new CourseStatVO.TeacherInfo(
                    c.getTeacher() != null ? c.getTeacher().getUsername() : ""));
            item.setCredits(c.getCredits());
            item.setCapacity(c.getCapacity());
            item.setSelectedCount(selectedCount);
            item.setSelectedRate(c.getCapacity() > 0
                    ? Math.round(selectedCount * 1000.0 / c.getCapacity()) / 10.0 : 0);
            item.setSemester(c.getSemester());
            items.add(item);

            totalSelected += selectedCount;
            if (c.getCapacity() > 0 && selectedCount >= c.getCapacity()) {
                hotCourseCount++;
            }
        }

        // 分页
        long total = items.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        List<CourseStatVO.CourseStatItem> pagedItems = fromIndex < items.size()
                ? items.subList(fromIndex, toIndex) : new ArrayList<>();

        // 汇总
        CourseStatVO.StatSummary summary = new CourseStatVO.StatSummary();
        summary.setTotalCourse(filtered.size());
        summary.setTotalSelected(totalSelected);
        summary.setAverageSelected(filtered.isEmpty()
                ? 0 : Math.round(totalSelected * 10.0 / filtered.size()) / 10.0);
        summary.setHotCourseCount(hotCourseCount);

        CourseStatVO vo = new CourseStatVO();
        vo.setSummary(summary);
        vo.setList(pagedItems);
        vo.setTotal(total);

        return ResponseEntity.ok(ApiResponse.success(vo));
    }

    // ==================== 导出占位 ====================

    @GetMapping("/courses/stats/export")
    public ResponseEntity<byte[]> exportCourseStat() {
        String csv = "\u8BFE\u7A0B\u540D\u79F0,\u6559\u5E08,\u5B66\u5206,\u5BB9\u91CF,\u5DF2\u9009,\u9009\u8BFE\u7387\n"
                + "\u5BFC\u51FA\u529F\u80FD\u5F85\u5B9E\u73B0,,,,,\n";
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition", "attachment; filename=course_stat.xlsx")
                .body(bytes);
    }
}
