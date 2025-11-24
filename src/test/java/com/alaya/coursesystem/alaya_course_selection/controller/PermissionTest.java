// 修正后的PermissionTest.java
package com.alaya.coursesystem.alaya_course_selection.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionTest {

    @Autowired
    private MockMvc mockMvc;

    // 测试学生访问教师接口：预期403（Forbidden）
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT") // 补充用户名，避免默认值
    public void testStudentAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isForbidden()); // 核心：学生无权限，返回403
    }

    @Test
    public void testAnonymousAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isUnauthorized()); // 未登录返回401
    }

    // 测试教师访问教师接口：预期200（Ok）
    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER") // 补充用户名
    public void testTeacherAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isOk()); // 接口存在且有权限，返回200
    }
}