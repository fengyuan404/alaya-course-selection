package com.alaya.coursesystem.alaya_course_selection.controller;

import com.alaya.coursesystem.alaya_course_selection.entity.UserRole;
import com.alaya.coursesystem.alaya_course_selection.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 用@Data注解自动生成getter/setter/toString等方法（需确保引入lombok依赖）
@Data
public class RegisterRequest {
    // 用户名非空校验
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 密码非空 + 强度校验
    @NotBlank(message = "密码不能为空")
    @StrongPassword(message = "密码需包含字母和数字，长度不少于6位")
    private String password;

    // 邮箱非空 + 格式校验
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;

    // 角色（枚举类型，自动校验合法性）
    private UserRole role;
}