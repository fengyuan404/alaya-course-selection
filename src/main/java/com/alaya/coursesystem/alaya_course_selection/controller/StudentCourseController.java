package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler.ApiResponse;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
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
    private final CourseSelectionRepository selectionRepository;

    // ========== 原有接口（补充泛型） ==========
    // 学生选课
    @PostMapping("/select/{courseId}")
    // 补充泛型：ApiResponse<CourseSelection>
    public ResponseEntity<ApiResponse<CourseSelection>> selectCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        CourseSelection selection = selectionService.selectCourse(courseId, authentication);
        return ResponseEntity.ok(ApiResponse.success(selection));
    }

    // 学生退课
    @DeleteMapping("/drop/{selectionId}")
    // 补充泛型：ApiResponse<Void>（无返回数据）
    public ResponseEntity<ApiResponse<Void>> dropCourse(
            @PathVariable Long selectionId,
            Authentication authentication) {
        selectionService.withdrawCourse(selectionId, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 查询已选课程
    @GetMapping("/selected")
    // 补充泛型：ApiResponse<List<CourseSelection>>
    public ResponseEntity<ApiResponse<List<CourseSelection>>> getSelectedCourses(Authentication authentication) {
        List<CourseSelection> selectedCourses = selectionService.getMySchedule(authentication);
        return ResponseEntity.ok(ApiResponse.success(selectedCourses));
    }

    // 学生查看单门课程详情
    @GetMapping("/{id}")
    // 补充泛型：ApiResponse<Course>
    public ResponseEntity<ApiResponse<Course>> getCourseDetail(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    // ========== 分页接口修改（核心修复） ==========
    // 1. 学生分页查询所有课程（基础列表）
    @GetMapping
    // 补充泛型：ApiResponse<PageResponseVO<Course>>
    public ResponseEntity<ApiResponse<PageResponseVO<Course>>> getCoursesByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer credits
    ) {
        // 手动构建PageRequestDTO（避免绑定失败）
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);
        pageRequest.setKeyword(keyword);
        pageRequest.setCredits(credits);

        PageResponseVO<Course> coursePage = courseService.getAllCoursesByPage(pageRequest);

        // 为每个课程填充 selectedCount 和 alreadySelected
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context
                .SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User currentUser) {
            for (Course course : coursePage.getList()) {
                long count = selectionRepository.countByCourseAndStatus(course, "SELECTED");
                course.setSelectedCount((int) count);
                // 只查 SELECTED 状态的选课记录，避免已退课程误判
                java.util.Optional<com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection> sel =
                    selectionRepository.findByUserAndCourse(currentUser, course);
                boolean selected = sel.isPresent() && "SELECTED".equals(sel.get().getStatus());
                course.setAlreadySelected(selected);
            }
        } else {
            // 未认证时只填充 selectedCount
            for (Course course : coursePage.getList()) {
                long count = selectionRepository.countByCourseAndStatus(course, "SELECTED");
                course.setSelectedCount((int) count);
                course.setAlreadySelected(false);
            }
        }

        // 统一引用ApiResponse，避免UnifiedExceptionHandler.ApiResponse冗余
        return ResponseEntity.ok(ApiResponse.success(coursePage));
    }

    // 2. 学生模糊搜索课程（支持名称/教师/学分筛选）
    @GetMapping("/search")
    // 补充泛型：ApiResponse<PageResponseVO<Course>>
    public ResponseEntity<ApiResponse<PageResponseVO<Course>>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer credits,
            @Valid PageRequestDTO pageRequest,
            // 新增BindingResult处理参数绑定错误，避免直接抛400
            BindingResult bindingResult) {

        // 处理参数绑定错误（返回友好提示，而非默认400）
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, errorMsg));
        }

        // 手动给pageRequest补充搜索参数（避免参数分散）
        pageRequest.setKeyword(keyword);
        pageRequest.setCredits(credits);

        PageResponseVO<Course> coursePage = courseService.searchCourses(keyword, credits, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(coursePage));
    }
}