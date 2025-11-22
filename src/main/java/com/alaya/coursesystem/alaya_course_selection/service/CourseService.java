package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSelectionRepository selectionRepository;

    // 优化：使用分页工具类替换原有分页方法
    @Cacheable(value = "courseList", key = "'all_' + #pageRequest.pageNum + '_' + #pageRequest.pageSize")
    public PageResponseVO<Course> getAllCoursesByPage(PageRequestDTO pageRequest) {
        // 按id降序排序（保持原有排序逻辑）
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Page<Course> coursePage = courseRepository.findAll(pageRequest.toPageable(sort));
        return PageResponseVO.from(coursePage);
    }

    // 优化：使用分页工具类处理搜索分页
    @Cacheable(value = "courseList", key = "'search_' + #keyword + '_' + #credits + '_' + #pageRequest.pageNum + '_' + #pageRequest.pageSize")
    public PageResponseVO<Course> searchCourses(String keyword, Integer credits, PageRequestDTO pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 保持原有排序逻辑

        // 动态构建查询条件（保留原有搜索逻辑）
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 课程名称模糊搜索
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }
            // 教师姓名模糊搜索（关联User表的username）
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("teacher").get("username"), "%" + keyword + "%"));
            }
            // 学分筛选
            if (credits != null) {
                predicates.add(cb.equal(root.get("credits"), credits));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageRequest.toPageable(sort));
        return PageResponseVO.from(coursePage);
    }

    // 原有方法保留
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @CacheEvict(value = "courseList", allEntries = true)
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    @Cacheable(value = "courseList", key = "#id")
    public Course getCourseById(Long id) {
        // 优化：使用统一异常处理器替换RuntimeException
        return courseRepository.findById(id)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在：" + id));
    }

    public boolean hasCapacity(Course course) {
        long selectedCount = selectionRepository.findByCourse(course).size();
        return selectedCount < course.getCapacity();
    }

    @CacheEvict(value = "courseList", allEntries = true)
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        long selectedCount = selectionRepository.findByCourse(course).size();
        if (selectedCount > 0) {
            // 优化：使用统一异常处理器
            throw new UnifiedExceptionHandler.BusinessException("该课程已有学生选课（" + selectedCount + "人），无法删除");
        }
        courseRepository.deleteById(id);
    }

    // 原有方法保留
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }
}