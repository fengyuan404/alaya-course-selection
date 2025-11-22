package com.alaya.coursesystem.alaya_course_selection.test;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import com.alaya.coursesystem.alaya_course_selection.service.CourseSelectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 确保测试数据在事务内有效，测试后自动回滚
public class CourseSelectionTest {

    @Autowired
    private CourseSelectionService selectionService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseSelectionRepository selectionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器

    private User student2;
    private User teacher1;
    private Course fullCourse; // 容量1的课程
    private Course conflictCourse; // 时间冲突的课程
    private Authentication auth1; // student1的认证信息

    @BeforeEach
    void setUp() {
        // 1. 初始化教师（用于创建课程）
        teacher1 = userRepository.findByUsername("test_teacher")
                .orElseGet(() -> {
                    User t = new User();
                    t.setUsername("test_teacher");
                    t.setEmail("teacher_test@example.com");
                    t.setPassword(passwordEncoder.encode("123456")); // 加密存储
                    //t.setName("测试教师");
                    t.setRole(UserRole.TEACHER); // 必须指定角色
                    return userRepository.save(t);
                });

        // 2. 初始化学生1
        // 必须指定角色
        // 测试数据
        User student1 = userRepository.findByUsername("test_student1")
                .orElseGet(() -> {
                    User s = new User();
                    s.setUsername("test_student1");
                    s.setEmail("student1_test@example.com");
                    s.setPassword(passwordEncoder.encode("123456"));
                    //s.setName("测试学生1");
                    s.setRole(UserRole.STUDENT); // 必须指定角色
                    return userRepository.save(s);
                });

        // 3. 初始化学生2
        student2 = userRepository.findByUsername("test_student2")
                .orElseGet(() -> {
                    User s = new User();
                    s.setUsername("test_student2");
                    s.setPassword(passwordEncoder.encode("123456"));
                    s.setEmail("student2_test@example.com");
                //    s.setName("测试学生2");
                    s.setRole(UserRole.STUDENT);
                    return userRepository.save(s);
                });

        // 4. 创建学生1的认证对象（含权限信息）
        auth1 = new UsernamePasswordAuthenticationToken(
                student1,
                null,
                student1.getAuthorities() // 必须传入权限（从Role转换）
        );

        // 5. 创建容量1的课程（时间：周一 1-2节）
        fullCourse = new Course();
        fullCourse.setName("容量测试课程");
        fullCourse.setCapacity(1); // 容量1
        fullCourse.setCredits(2);
        fullCourse.setSchedule("周一 1-2节"); // 符合时间解析格式
        fullCourse.setLocation("测试教室1");
        fullCourse.setTeacher(teacher1); // 关联教师
        fullCourse = courseRepository.save(fullCourse);

        // 6. 创建时间冲突的课程（同时间：周一 1-2节）
        conflictCourse = new Course();
        conflictCourse.setName("冲突测试课程");
        conflictCourse.setCapacity(10);
        conflictCourse.setCredits(2);
        conflictCourse.setSchedule("周一 1-2节"); // 与fullCourse时间冲突
        conflictCourse.setLocation("测试教室2");
        conflictCourse.setTeacher(teacher1);
        conflictCourse = courseRepository.save(conflictCourse);

        // 清理残留数据（避免干扰）
        //selectionRepository.deleteByCourse(fullCourse);
        //selectionRepository.deleteByCourse(conflictCourse);
    }

    // 测试1：课程满员时无法选课
    @Test
    void testSelectFullCourse() {
        // 学生1选课（预期成功）
        selectionService.selectCourse(fullCourse.getId(), auth1);

        // 学生2选课（预期失败：容量不足）
        Authentication auth2 = new UsernamePasswordAuthenticationToken(student2, null, student2.getAuthorities());
        UnifiedExceptionHandler.BusinessException exception = assertThrows(
                UnifiedExceptionHandler.BusinessException.class,
                () -> selectionService.selectCourse(fullCourse.getId(), auth2)
        );
        assertTrue(exception.getMessage().contains("已满员"));
    }

    // 测试2：重复选课失败
    @Test
    void testRepeatSelect() {
        // 第一次选课（成功）
        selectionService.selectCourse(fullCourse.getId(), auth1);

        // 第二次选同一课程（失败）
        UnifiedExceptionHandler.BusinessException exception = assertThrows(
                UnifiedExceptionHandler.BusinessException.class,
                () -> selectionService.selectCourse(fullCourse.getId(), auth1)
        );
        assertTrue(exception.getMessage().contains("已选课程"));
    }

    // 测试3：时间冲突选课失败
    @Test
    void testTimeConflict() {
        // 先选fullCourse
        selectionService.selectCourse(fullCourse.getId(), auth1);

        // 再选冲突课程（失败）
        UnifiedExceptionHandler.BusinessException exception = assertThrows(
                UnifiedExceptionHandler.BusinessException.class,
                () -> selectionService.selectCourse(conflictCourse.getId(), auth1)
        );
        assertTrue(exception.getMessage().contains("时间冲突"));
    }

    // 测试4：退课后容量释放
    @Test
    void testWithdrawCourseReleaseCapacity() {
        // 学生1选课
        CourseSelection selection = selectionService.selectCourse(fullCourse.getId(), auth1);

        // 学生1退课
        selectionService.withdrawCourse(selection.getId(), auth1);

        // 学生2选课（应成功）
        Authentication auth2 = new UsernamePasswordAuthenticationToken(student2, null, student2.getAuthorities());
        assertDoesNotThrow(() -> selectionService.selectCourse(fullCourse.getId(), auth2));
    }
}