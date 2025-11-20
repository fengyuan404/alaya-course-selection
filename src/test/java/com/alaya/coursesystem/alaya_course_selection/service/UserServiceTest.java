// 包路径：com.alaya.coursesystem.alaya_course_selection.service
// 注意：测试类应放在src/test/java下（而非src/main/java），否则依赖不会生效
package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*; // 补全Assertions导入

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired // 自动注入（解决“从未分配”提示）
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegisterStudent() {
        // 准备测试数据
        User user = new User();
        user.setUsername("test_stu");
        user.setPassword("Stu123456");
        user.setEmail("test_stu@alaya.edu");
        user.setRole(UserRole.STUDENT);

        // 执行注册
        User savedUser = userService.save(user);

        // 断言结果（补全方法调用）
        assertNotNull(savedUser.getId());
        assertTrue(passwordEncoder.matches("Stu123456", savedUser.getPassword()));
        assertEquals(UserRole.STUDENT, savedUser.getRole());
    }
}