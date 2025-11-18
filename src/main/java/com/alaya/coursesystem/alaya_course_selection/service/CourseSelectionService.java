// 包路径：com.alaya.coursesystem.alaya_course_selection.service
package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseSelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final CourseService courseService;

    // 构造器注入（替代@RequiredArgsConstructor，避免冗余依赖）
    public CourseSelectionService(CourseSelectionRepository selectionRepository, CourseService courseService) {
        this.selectionRepository = selectionRepository;
        this.courseService = courseService;
    }

    // 选课（从Authentication获取当前用户）
    @Transactional
    public CourseSelection selectCourse(Long courseId, Authentication authentication) {
        // 从登录状态获取当前用户
        User currentUser = (User) authentication.getPrincipal();
        // 获取课程信息
        Course course = courseService.getCourseById(courseId);

        // 校验：是否已选该课程
        Optional<CourseSelection> existingSelection = selectionRepository.findByUserAndCourse(currentUser, course);
        if (existingSelection.isPresent()) {
            throw new RuntimeException("已选该课程：" + course.getName());
        }

        // 校验：课程是否有剩余容量
        if (!courseService.hasCapacity(course)) {
            throw new RuntimeException("课程容量不足：" + course.getName());
        }

        // 执行选课
        CourseSelection selection = new CourseSelection();
        selection.setUser(currentUser);
        selection.setCourse(course);
        return selectionRepository.save(selection);
    }

    // 退课
    @Transactional
    public void dropCourse(Long selectionId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        // 校验：选课记录是否存在且属于当前用户
        CourseSelection selection = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在：" + selectionId));
        if (!selection.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作他人的选课记录");
        }

        selectionRepository.delete(selection);
    }

    // 查询已选课程
    public List<CourseSelection> getSelectedCourses(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return selectionRepository.findByUser(currentUser);
    }
}