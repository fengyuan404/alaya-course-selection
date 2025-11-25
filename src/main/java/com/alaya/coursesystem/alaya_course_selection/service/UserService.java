// 包路径：com.alaya.coursesystem.alaya_course_selection.service
package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;




    // 确保存在这个save方法（参数是User，返回User）
    public User save(User user) {
        // （可选）如果是更新用户，不需要重复加密密码（避免覆盖原加密密码）
        // 仅在新建用户时加密，更新时跳过
        if (user.getId() == null) { // 新建用户（无ID）
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // 用户注册
    public User register(String username, String password, String email, UserRole role) {
        // 校验用户名和邮箱是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在：" + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在：" + email);
        }
        // 密码加密
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt加密
        user.setEmail(email);
        user.setRole(Role.valueOf(role.name()));
        return userRepository.save(user);


    }

    // Spring Security登录时调用：根据用户名查询用户
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));
    }
}