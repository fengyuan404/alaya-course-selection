// 包路径不变，修改AuthController内容
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.service.UserService;
import com.alaya.coursesystem.alaya_course_selection.validator.StrongPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        // 先注释密码强度注解，测试基础功能
        @NotBlank(message = "密码不能为空")
        @StrongPassword(message = "密码需包含字母和数字，长度不少于6位")
        private String password;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式错误")
        private String email;

        private UserRole role;
    }

    // 核心修改：添加BindingResult捕获校验错误
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult // 新增：捕获校验结果
    ) {
        // 1. 手动检查校验错误
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            // 直接返回400 + 错误信息
            return ResponseEntity.badRequest().body(new ErrorResponse(400, errorMsg));
        }

        // 2. 核心注册逻辑
        try {
            User user = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getRole()
            );
            user.setPassword(null);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            // 捕获业务异常
            return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
        }
    }

    // 临时错误响应类（控制器内部）
    @Data
    static class ErrorResponse {
        private int code;
        private String message;

        public ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    // 获取当前用户信息方法不变
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}