package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 课程数据访问接口
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // 可自定义查询方法，如根据课程名称查询
    Course findByName(String name);
}