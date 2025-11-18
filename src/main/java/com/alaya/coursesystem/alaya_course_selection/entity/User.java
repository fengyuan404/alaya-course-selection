// 包路径：com.alaya.coursesystem.alaya_course_selection.entity
package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // 实现UserDetails，适配Spring Security
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 以下为UserDetails接口实现（安全认证用）
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 角色需加"ROLE_"前缀，符合Spring Security规范
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账号未过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账号未锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 密码未过期
    }

    @Override
    public boolean isEnabled() {
        return true; // 账号启用
    }
}