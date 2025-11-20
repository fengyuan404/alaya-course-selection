// 包路径：com.alaya.coursesystem.alaya_course_selection.entity
package com.alaya.coursesystem.alaya_course_selection.entity;

public enum UserRole {
    STUDENT("ROLE_STUDENT"), // Spring Security角色默认前缀为ROLE_
    TEACHER("ROLE_TEACHER"),
    ADMIN("ROLE_ADMIN");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}