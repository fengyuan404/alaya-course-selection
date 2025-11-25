package com.alaya.coursesystem.alaya_course_selection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

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

    // 核心：只保留Role类型的持久化字段（和数据库映射）
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    // ========== 修复Jackson冲突：删除重载的setRole(UserRole)，改用专用转换方法 ==========
    // 原重载的setRole(UserRole)删除，避免方法名冲突
    // 新增非setter命名的转换方法，兼容UserRole -> Role
    @JsonIgnore // 让Jackson忽略这个方法，避免扫描
    public void convertFromUserRole(UserRole userRole) {
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
                this.role = Role.STUDENT;
        }
    }

    // ========== 兼容Spring Security权限逻辑 ==========
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 从Role转换为UserRole的ROLE_前缀，适配Spring Security
        UserRole userRole = UserRole.valueOf(this.role.name());
        return Collections.singletonList(new SimpleGrantedAuthority(userRole.getRoleName()));
    }

    // ========== UserDetails接口默认实现 ==========
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

    // ========== 可选：新增获取UserRole的方法（方便业务调用） ==========
    @JsonIgnore // 避免Jackson序列化时重复解析
    public UserRole getUserRole() {
        return UserRole.valueOf(this.role.name());
    }
}