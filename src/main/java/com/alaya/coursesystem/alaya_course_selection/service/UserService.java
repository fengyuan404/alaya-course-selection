// 包路径：com.alaya.coursesystem.alaya_course_selection.service
package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User save(User user) {
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User register(String username, String password, String email, com.alaya.coursesystem.alaya_course_selection.entity.UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在：" + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在：" + email);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.valueOf(role.name()));
        return userRepository.save(user);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));
    }

    // ========== 管理员方法 ==========

    /** 分页查询用户（带关键词+角色筛选） */
    public Page<User> getUsersPage(String keyword, String role, int pageNum, int pageSize) {
        Role roleEnum = (role != null && !role.isEmpty()) ? Role.valueOf(role.toUpperCase()) : null;
        return userRepository.findByKeywordAndRole(keyword, roleEnum, PageRequest.of(pageNum - 1, pageSize));
    }

    /** 管理员创建用户 */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在：" + user.getUsername());
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在：" + user.getEmail());
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /** 管理员更新用户 */
    @Transactional
    public User updateUser(Long id, User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        existing.setUsername(updated.getUsername());
        existing.setRole(updated.getRole());
        existing.setIdCard(updated.getIdCard());
        existing.setEmail(updated.getEmail());
        // 不更新密码
        return userRepository.save(existing);
    }

    /** 管理员删除用户 */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("不能删除管理员用户");
        }
        userRepository.deleteById(id);
    }

    /** 管理员重置密码 */
    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    /** 获取教师列表 */
    public List<User> getTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }
}
