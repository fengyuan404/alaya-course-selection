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
    // 按课程+状态统计已选人数（容量校验核心）
    long countByCourseAndStatus(Course course, String status);

    // 按学生+课程查询选课记录（防重复选课）
    Optional<CourseSelection> findByUserAndCourse(User user, Course course);

    // 按学生+状态查询选课记录（冲突检测/课表查询）
    List<CourseSelection> findByUserAndStatus(User user, String status);

    // 按学生查询所有选课记录（可选）
    List<CourseSelection> findByUser(User user);
    List<CourseSelection> findByCourse(Course course);
}