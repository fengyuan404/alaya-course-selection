package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.exception.CourseSystemExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    // 管理员查询所有用户
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // 调试：打印当前认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("当前认证信息：" + auth); // 正常应为UsernamePasswordAuthenticationToken，包含用户信息
        System.out.println("是否已认证：" + (auth != null && auth.isAuthenticated()));

        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // 管理员修改用户角色
    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID：" + id));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        updatedUser.setPassword(null);

        return ResponseEntity.ok(updatedUser);
    }

    // 管理员删除用户
    @DeleteMapping("/users/{id}")
    public ResponseEntity<CourseSystemExceptionHandler.ErrorResponse> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在，ID：" + id);
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(new CourseSystemExceptionHandler.ErrorResponse(200, "用户删除成功，ID：" + id));
    }
}