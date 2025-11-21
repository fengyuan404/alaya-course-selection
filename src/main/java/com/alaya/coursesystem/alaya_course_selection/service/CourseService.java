package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // 新增：分页查询所有课程
    public Page<Course> getAllCoursesByPage(int pageNum, int pageSize) {
        // 按id降序排序
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort); // pageNum从1开始，PageRequest从0开始
        return courseRepository.findAll(pageable);
    }

    // 新增：课程模糊搜索（支持名称/教师姓名/学分筛选）
    public Page<Course> searchCourses(String keyword, Integer credits, int pageNum, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);

        // 动态构建查询条件
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

        return courseRepository.findAll(spec, pageable);
    }

    // 原有方法保留，补充注释
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在：" + id));
    }

    public boolean hasCapacity(Course course) {
        long selectedCount = selectionRepository.findByCourse(course).size();
        return selectedCount < course.getCapacity();
    }

    public void deleteCourse(Long id) {
        // 新增：删除前检查是否有学生选课（有则禁止删除）
        Course course = getCourseById(id);
        long selectedCount = selectionRepository.findByCourse(course).size();
        if (selectedCount > 0) {
            throw new RuntimeException("该课程已有学生选课，无法删除");
        }
        courseRepository.deleteById(id);
    }

    // 替换原按teacherName查询，改为按teacherId查询
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }
}