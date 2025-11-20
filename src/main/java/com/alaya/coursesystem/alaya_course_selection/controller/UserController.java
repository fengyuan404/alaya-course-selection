// 包路径：com.alaya.coursesystem.alaya_course_selection.controller
package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // 已使用的接口（保留）
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO dto,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // 校验原密码
        if (dto.getNewPassword() != null) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), currentUser.getPassword())) {
                throw new RuntimeException("原密码错误");
            }
            currentUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        // 更新邮箱
        if (dto.getEmail() != null) {
            currentUser.setEmail(dto.getEmail());
        }

        User updatedUser = userService.save(currentUser);
        updatedUser.setPassword(null);
        return ResponseEntity.ok(updatedUser);
    }

    // 修复后的UserUpdateDTO
    @Data
    public static class UserUpdateDTO {
        @Email(message = "邮箱格式错误")
        private String email;
        private String oldPassword;
        private String newPassword;
    }

}