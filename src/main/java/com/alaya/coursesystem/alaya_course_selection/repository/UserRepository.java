// 包路径：com.alaya.coursesystem.alaya_course_selection.repository
package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // 登录时查询用户
    boolean existsByUsername(String username); // 注册时校验用户名唯一
    boolean existsByEmail(String email); // 注册时校验邮箱唯一
}