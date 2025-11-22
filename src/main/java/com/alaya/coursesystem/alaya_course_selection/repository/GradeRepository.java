package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.Grade;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    // 根据选课记录查询成绩
    Optional<Grade> findBySelectionId(Long selectionId);

    // 根据学生查询所有成绩
    List<Grade> findBySelection_UserOrderBySelection_Course_Name(User student);

    // 根据课程查询所有成绩
    List<Grade> findBySelection_Course(Course course);

        // 学生按学期查成绩
        List<Grade> findBySelection_User_IdAndSelection_Semester(Long studentId, String semester);
        // 教师按课程+学期查成绩
        List<Grade> findBySelection_Course_IdAndSelection_Semester(Long courseId, String semester);

}