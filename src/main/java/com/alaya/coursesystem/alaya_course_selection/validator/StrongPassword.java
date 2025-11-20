// 修正StrongPassword.java的导入
package com.alaya.coursesystem.alaya_course_selection.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload; // 替换javax为jakarta
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "密码需包含字母和数字，长度不少于6位";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}