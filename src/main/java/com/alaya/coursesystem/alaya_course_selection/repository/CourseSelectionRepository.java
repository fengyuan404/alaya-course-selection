// 包路径：com.alaya.coursesystem.alaya_course_selection.repository
package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    // 查询用户已选课程
    List<CourseSelection> findByUser(User user);

    // 查询课程的选课记录（用于统计人数）
    List<CourseSelection> findByCourse(Course course);

    // 检查用户是否已选该课程（避免重复选课）
    Optional<CourseSelection> findByUserAndCourse(User user, Course course);
}