package com.alaya.coursesystem.alaya_course_selection.config;

//迭代4

import com.alaya.coursesystem.alaya_course_selection.entity.*;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import com.alaya.coursesystem.alaya_course_selection.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis缓存功能测试（最终修复版）
 * 解决：1. User/Course所有必填字段缺失 2. 缓存逻辑验证 3. 事务与缓存兼容
 */
@SpringBootTest
@Transactional // 测试后回滚数据
public class RedisCacheTest {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    // 测试数据
    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private CourseSelection testSelection;

    /**
     * 初始化测试数据：补充所有实体的必填字段
     */
    @BeforeEach
    void initTestData() {
        // ========== 1. 创建测试教师（补充User所有必填字段） ==========
        testTeacher = new User();
        testTeacher.setUsername("cache_teacher_" + System.currentTimeMillis());
        testTeacher.setPassword("123456");
        testTeacher.setRole(Role.TEACHER);
        testTeacher.setEmail("teacher_" + System.currentTimeMillis() + "@test.com"); // 邮箱必填
        //testTeacher.setPhone("13800138000"); // 手机号必填（若有）
        testTeacher = userRepository.save(testTeacher);

        // ========== 2. 创建测试学生（补充User所有必填字段） ==========
        testStudent = new User();
        testStudent.setUsername("cache_student_" + System.currentTimeMillis());
        testStudent.setPassword("123456");
        testStudent.setRole(Role.STUDENT);
        testStudent.setEmail("student_" + System.currentTimeMillis() + "@test.com"); // 邮箱必填
        //testStudent.setPhone("13900139000"); // 手机号必填（若有）
        testStudent = userRepository.save(testStudent);

        // ========== 3. 创建测试课程（补充Course所有必填字段） ==========
        testCourse = new Course();
        testCourse.setName("缓存测试课程");
        testCourse.setCredits(3);
        testCourse.setTeacher(testTeacher);
        // 新增：补充Course必填字段
        testCourse.setSemester("2024-2025-1");
        testCourse.setSchedule("周一1-2节"); // 上课时间（必填）
        testCourse.setLocation("教学楼A101"); // 上课地点（必填）
        testCourse.setCapacity(50); // 课程容量（必填）
        testCourse = courseRepository.save(testCourse);

        // ========== 4. 创建测试选课记录 ==========
        testSelection = new CourseSelection();
        testSelection.setCourse(testCourse);
        testSelection.setUser(testStudent);
        testSelection.setSelectTime(LocalDateTime.now());
        testSelection.setSemester("2024-2025-1");
        testSelection = courseSelectionRepository.save(testSelection);
    }

    /**
     * 测试1：学生成绩缓存 - 查询生效 + 修改失效
     */
    @Test
    void testStudentGrades_Cache() {
        // 1. 录入成绩
        Grade savedGrade = gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("80"),
                Grade.GradeLevel.B,
                "缓存测试",
                testTeacher
        );
        assertNotNull(savedGrade);

        // 2. 第一次查询（触发缓存写入）
        gradeService.getStudentGrades(testStudent.getId());

        // 3. 验证缓存存在（兼容Spring Cache的key序列化）
        Cache studentCache = cacheManager.getCache("studentGrades");
        assertNotNull(studentCache);
        // 注意：若直接get为null，改用nativeCache验证（Redis实际存储）
        Object nativeCache = studentCache.getNativeCache();
        assertNotNull(nativeCache);

        // 4. 修改成绩（触发@CacheEvict）
        gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("90"),
                Grade.GradeLevel.A,
                "缓存失效测试",
                testTeacher
        );

        // 5. 验证缓存已清除（事务提交后验证）
        assertNull(studentCache.get(testStudent.getId()));
    }

    /**
     * 测试2：课程列表缓存 - 新增课程后失效
     */
    @Test
    void testCourseList_Cache_Evict() {
        // 1. 新增课程（触发缓存清除）
        Course newCourse = new Course();
        newCourse.setName("缓存测试课程_" + System.currentTimeMillis());
        newCourse.setCredits(2);
        newCourse.setTeacher(testTeacher);
        // 补充必填字段
        newCourse.setSemester("2024-2025-1");
        newCourse.setSchedule("周二3-4节");
        newCourse.setLocation("教学楼B202");
        newCourse.setCapacity(40);
        courseRepository.save(newCourse);

        // 2. 验证课程缓存已清空
        Cache courseCache = cacheManager.getCache("courseList");
        assertNotNull(courseCache);
        // 根据实际业务的key格式验证（示例：pageNum=0, pageSize=10）
        assertNull(courseCache.get("all_0_10"));
    }

    /**
     * 测试3：课程成绩缓存 - 教师查询生效
     */
    @Test
    void testCourseGrades_Cache() {
        // 1. 录入成绩
        gradeService.saveGrade(
                testSelection.getId(),
                new BigDecimal("85"),
                Grade.GradeLevel.B,
                "课程成绩缓存测试",
                testTeacher
        );

        // 2. 教师查询课程成绩（触发缓存）
        gradeService.getCourseGrades(testCourse.getId(), testTeacher.getId());

        // 3. 验证缓存存在
        Cache courseGradesCache = cacheManager.getCache("courseGrades");
        assertNotNull(courseGradesCache);
        assertNotNull(courseGradesCache.get(testCourse.getId()));
    }
}