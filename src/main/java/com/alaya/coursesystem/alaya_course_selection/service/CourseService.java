// 包路径：com.alaya.coursesystem.alaya_course_selection.service
package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSelectionRepository selectionRepository;

    // 获取所有课程
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // 添加课程（教师/管理员）
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    // 根据ID查询课程
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在：" + id));
    }

    // 检查课程是否还有容量
    public boolean hasCapacity(Course course) {
        long selectedCount = selectionRepository.findByCourse(course).size();
        return selectedCount < course.getCapacity();
    }
}