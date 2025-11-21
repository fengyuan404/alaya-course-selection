package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    // 根据用户和课程查询选课记录
    Optional<CourseSelection> findByUserAndCourse(User user, Course course);
    // 根据用户查询所有选课记录
    List<CourseSelection> findByUser(User user);
    // 根据课程查询所有选课记录
    List<CourseSelection> findByCourse(Course course);
}