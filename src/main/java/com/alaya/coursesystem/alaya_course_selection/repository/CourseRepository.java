package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByName(String name);
    // 新增：根据教师查询课程
    List<Course> findByTeacherName(String teacherName);
}