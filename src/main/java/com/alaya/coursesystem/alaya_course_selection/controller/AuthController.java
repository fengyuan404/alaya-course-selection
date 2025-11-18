// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// 注册请求参数DTO
record RegisterRequest(
        @Valid String username,
        @Valid String password,
        @Valid String email,
        @Valid Role role
) {}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
                request.username(),
                request.password(),
                request.email(),
                request.role()
        );
        user.setPassword(null); // 隐藏密码
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // 获取当前登录用户信息
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal(); // 从Security上下文获取用户
        user.setPassword(null); // 隐藏密码
        return ResponseEntity.ok(user);
    }
}