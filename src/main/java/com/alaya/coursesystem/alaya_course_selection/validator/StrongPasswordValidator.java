// 修正StrongPasswordValidator.java的导入
package com.alaya.coursesystem.alaya_course_selection.validator;

import jakarta.validation.ConstraintValidator; // 替换javax为jakarta
import jakarta.validation.ConstraintValidatorContext; // 替换javax为jakarta

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        // 密码规则：长度≥6，包含字母和数字
        return password.length() >= 6
                && password.matches(".*[a-zA-Z].*")
                && password.matches(".*\\d.*");
    }
}