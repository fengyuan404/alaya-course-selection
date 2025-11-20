// 包路径不变，修改AuthController内容
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.service.UserService;
import com.alaya.coursesystem.alaya_course_selection.validator.StrongPassword;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        try {
            // 1. 校验用户名和密码（通过Spring Security的AuthenticationManager）
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            // 2. 将认证信息存入Security上下文（维持登录态）
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("登录成功 - 认证信息：" + authentication);
            System.out.println("登录成功 - SecurityContext中的认证：" + SecurityContextHolder.getContext().getAuthentication());

            // 3. 返回当前用户信息（隐藏密码）
            User user = (User) authentication.getPrincipal();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            // 登录失败（用户名/密码错误）
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "用户名或密码错误"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ErrorResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 清空Security上下文
        SecurityContextHolder.clearContext();
        // 2. 使Session失效（若用Session认证）
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 3. 返回成功提示
        return ResponseEntity.ok(new ErrorResponse(200, "注销成功"));
    }

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