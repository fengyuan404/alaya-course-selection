package com.alaya.coursesystem.alaya_course_selection.config;

import com.alaya.coursesystem.alaya_course_selection.controller.GradeController;
import com.alaya.coursesystem.alaya_course_selection.entity.Grade;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.service.GradeService;
import com.alaya.coursesystem.alaya_course_selection.service.OperationLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // 关键：保留此包，但确保依赖兼容
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // 修正：使用Spring Boot 3.4+兼容的@MockBean
    @MockBean
    private GradeService gradeService;

    @MockBean
    private OperationLogService operationLogService;

    @Test
    @WithMockUser(username = "test_teacher", roles = {"TEACHER"})
    void testBusinessException_InvalidScore() throws Exception {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        User mockTeacher = new User();
        mockTeacher.setId(1L);
        mockTeacher.setRole(UserRole.TEACHER);
        when(auth.getPrincipal()).thenReturn(mockTeacher);

        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(gradeService.saveGrade(anyLong(), any(BigDecimal.class), any(Grade.GradeLevel.class), anyString(), any(User.class)))
                .thenThrow(new UnifiedExceptionHandler.BusinessException("分数必须在0-100之间"));

        mockMvc.perform(post("/api/grades")
                        .param("selectionId", "1")
                        .param("score", "101")
                        .param("level", "A")
                        .param("comment", "测试异常"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("error"))
                .andExpect(jsonPath("$.message").value("分数必须在0-100之间"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username = "test_student", roles = {"STUDENT"})
    void testAccessDeniedException() throws Exception {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        User mockStudent = new User();
        mockStudent.setId(2L);
        mockStudent.setRole(UserRole.STUDENT);
        when(auth.getPrincipal()).thenReturn(mockStudent);

        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(gradeService.saveGrade(anyLong(), any(BigDecimal.class), any(Grade.GradeLevel.class), anyString(), any(User.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("无权操作成绩"));

        mockMvc.perform(post("/api/grades")
                        .param("selectionId", "1")
                        .param("score", "80")
                        .param("level", "B")
                        .param("comment", "测试权限"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("error"))
                .andExpect(jsonPath("$.message").value("无权访问: 无权操作成绩"));

        SecurityContextHolder.clearContext();
    }
}