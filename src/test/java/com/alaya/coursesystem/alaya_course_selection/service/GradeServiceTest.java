package com.alaya.coursesystem.alaya_course_selection.service;

//迭代4测试

import com.alaya.coursesystem.alaya_course_selection.entity.*;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.GradeRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 测试后回滚数据，避免污染测试库
public class GradeServiceTest {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private CourseSelection testSelection;

    /**
     * 初始化测试数据：创建唯一的教师/学生/课程/选课记录
     */

    @BeforeEach
    void initTestData() {
        // 1. 创建测试教师（生成唯一email）
        testTeacher = new User();
        testTeacher.setUsername("test_teacher_" + UUID.randomUUID().toString().substring(0, 8));
        testTeacher.setPassword("123456");
        testTeacher.setRole(UserRole.TEACHER);
        testTeacher.setEmail("teacher_" + UUID.randomUUID() + "@alaya.edu"); // 唯一email
        testTeacher = userRepository.save(testTeacher);

        // 2. 创建测试学生（生成唯一email）
        testStudent = new User();
        testStudent.setUsername("test_student_" + UUID.randomUUID().toString().substring(0, 8));
        testStudent.setPassword("123456");
        testStudent.setRole(UserRole.STUDENT);
        testStudent.setEmail("student_" + UUID.randomUUID() + "@alaya.edu"); // 唯一email
        testStudent = userRepository.save(testStudent);

        // 3. 创建测试课程（关联教师 + 补全所有非空字段）
        testCourse = new Course();
        testCourse.setName("测试课程_" + UUID.randomUUID().toString().substring(0, 8));
        testCourse.setCredits(3);
        testCourse.setTeacher(testTeacher);
        // 核心补充：非空校验字段赋值
        testCourse.setLocation("教学楼A101"); // 上课地点
        testCourse.setCapacity(50); // 课程容量
        testCourse.setSchedule("周一10:00-12:00"); // 上课时间
        testCourse = courseRepository.save(testCourse);

        // 4. 创建测试选课记录
        testSelection = new CourseSelection();
        testSelection.setCourse(testCourse);
        testSelection.setUser(testStudent);
        //testSelection.setSelectTime(java.time.LocalDateTime.now()); // 已修正为selectTime
        testSelection.setSelectTime(LocalDateTime.now());
        testSelection = courseSelectionRepository.save(testSelection);
    }

    // ========== 原有测试方法保持不变 ==========
    @Test
    void testSaveGrade_Success() {
        Grade savedGrade = gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("85"),
                Grade.GradeLevel.B,
                "表现良好",
                testTeacher
        );

        assertNotNull(savedGrade.getId());
        assertEquals(new BigDecimal("85"), savedGrade.getScore());
        assertEquals(Grade.GradeLevel.B, savedGrade.getLevel());
        assertEquals("表现良好", savedGrade.getComment());
        assertEquals(testSelection.getId(), savedGrade.getSelection().getId());
    }

    @Test
    void testSaveGrade_Update() {
        gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("85"),
                Grade.GradeLevel.B,
                "初始评语",
                testTeacher
        );

        Grade updatedGrade = gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("90"),
                Grade.GradeLevel.A,
                "优秀",
                testTeacher
        );

        assertEquals(new BigDecimal("90"), updatedGrade.getScore());
        assertEquals(Grade.GradeLevel.A, updatedGrade.getLevel());
        assertEquals("优秀", updatedGrade.getComment());
    }

    @Test
    void testSaveGrade_InvalidScore() {
        UnifiedExceptionHandler.BusinessException exception = assertThrows(
                UnifiedExceptionHandler.BusinessException.class,
                () -> gradeService.saveGrade(
                        testSelection.getId(),
                        new BigDecimal("101"),
                        Grade.GradeLevel.A,
                        "无效分数",
                        testTeacher
                )
        );

        assertEquals("分数必须在0-100之间", exception.getMessage());
    }

    @Test
    void testSaveGrade_NoPermission() {
        User otherTeacher = new User();
        otherTeacher.setUsername("other_teacher_" + UUID.randomUUID().toString().substring(0, 8));
        otherTeacher.setPassword("123456");
        otherTeacher.setRole(UserRole.TEACHER);
        otherTeacher.setEmail("other_teacher_" + UUID.randomUUID() + "@alaya.edu");
        otherTeacher = userRepository.save(otherTeacher);

        User finalOtherTeacher = otherTeacher;
        UnifiedExceptionHandler.BusinessException exception = assertThrows(
                UnifiedExceptionHandler.BusinessException.class,
                () -> gradeService.saveGrade(
                        testSelection.getId(),
                        new BigDecimal("80"),
                        Grade.GradeLevel.B,
                        "无权限操作",
                        finalOtherTeacher
                )
        );

        assertEquals("无权操作其他教师的课程成绩", exception.getMessage());
    }

    @Test
    void testGetStudentGrades() {
        gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("85"),
                Grade.GradeLevel.B,
                "表现良好",
                testTeacher
        );

        List<Grade> studentGrades = gradeService.getStudentGrades(testStudent.getId());

        assertEquals(1, studentGrades.size());
        assertEquals(testStudent.getId(), studentGrades.get(0).getSelection().getUser().getId());
        assertEquals(testCourse.getId(), studentGrades.get(0).getSelection().getCourse().getId());
    }

    @Test
    void testGetCourseGrades() {
        gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("85"),
                Grade.GradeLevel.B,
                "表现良好",
                testTeacher
        );

        List<Grade> courseGrades = gradeService.getCourseGrades(testCourse.getId(), testTeacher.getId());

        assertEquals(1, courseGrades.size());
        assertEquals(testCourse.getId(), courseGrades.get(0).getSelection().getCourse().getId());
    }

    @Test
    void testAnalyzeCourseGrades() {
        // 学生1成绩
        gradeService.saveGrade(testSelection.getId(), new BigDecimal("85"), Grade.GradeLevel.B, "评语1", testTeacher);

        // 学生2
        User student2 = new User();
        student2.setUsername("student2_" + UUID.randomUUID().toString().substring(0, 8));
        student2.setRole(UserRole.STUDENT);
        student2.setPassword("123456");
        student2.setEmail("student2_" + UUID.randomUUID() + "@alaya.edu");
        student2 = userRepository.save(student2);

        CourseSelection selection2 = new CourseSelection();
        selection2.setCourse(testCourse);
        selection2.setUser(student2);
       // selection2.setSelectTime(java.time.LocalDateTime.now());
        selection2.setSelectTime(LocalDateTime.now());
        selection2 = courseSelectionRepository.save(selection2);
        gradeService.saveGrade(selection2.getId(), new BigDecimal("95"), Grade.GradeLevel.A, "评语2", testTeacher);

        Map<String, Object> analysis = gradeService.analyzeCourseGrades(testCourse.getId(), testTeacher.getId());

        assertEquals(2, analysis.get("totalStudents"));
        assertEquals(new BigDecimal("90.00"), analysis.get("averageScore"));

        Map<Grade.GradeLevel, Long> levelDistribution = (Map<Grade.GradeLevel, Long>) analysis.get("levelDistribution");
        assertEquals(1, levelDistribution.get(Grade.GradeLevel.A));
        assertEquals(1, levelDistribution.get(Grade.GradeLevel.B));
    }
}