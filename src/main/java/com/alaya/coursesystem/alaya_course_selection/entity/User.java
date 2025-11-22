package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Column(unique = true, nullable = false)
    private String email;

    // 保留原有的Role类型字段（不删除）
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;



    // 核心新增：重载setRole方法，适配UserRole类型
    public void setRole(UserRole userRole) {
        // 将UserRole转换为Role（根据枚举名称匹配）
        switch (userRole) {
            case STUDENT:
                this.role = Role.STUDENT;
                break;
            case TEACHER:
                this.role = Role.TEACHER;
                break;
            case ADMIN:
                this.role = Role.ADMIN;
                break;
            default:
                this.role = Role.STUDENT; // 默认学生
        }
    }

    // 保留原有的setRole方法（兼容Role类型）
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 若需要适配UserRole的ROLE_前缀，可修改此处：
        // 方式1：用原Role拼接ROLE_（兼容原有逻辑）
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // 方式2：若想使用UserRole的roleName，可手动映射（可选）
        /*
        UserRole userRole = UserRole.valueOf(role.name());
        return Collections.singletonList(new SimpleGrantedAuthority(userRole.getRoleName()));
        */
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}