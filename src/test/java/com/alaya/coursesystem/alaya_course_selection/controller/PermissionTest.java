// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
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

    // 测试学生无法访问教师接口
    @Test
    @WithMockUser(roles = "STUDENT")
    public void testStudentAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isForbidden()); // 断言403权限拒绝
    }

    // 测试教师可以访问自己的接口
    @Test
    @WithMockUser(roles = "TEACHER")
    public void testTeacherAccessTeacherApi() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isOk()); // 断言200成功
    }
}