package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course")
@Data // 添加lombok注解简化代码
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 课程名称

    @Column(name = "teacher_name", nullable = false)
    private String teacherName; // 授课教师

    private Integer capacity; // 课程容量

    private Integer credits; // 学分

    @Column(length = 500)
    private String description; // 课程描述

    private String schedule; // 上课时间

    private String location; // 上课地点

    // 无参构造器
    public Course() {}

    // 带参构造器
    public Course(String name, String teacherName, Integer capacity,
                  Integer credits, String description, String schedule, String location) {
        this.name = name;
        this.teacherName = teacherName;
        this.capacity = capacity;
        this.credits = credits;
        this.description = description;
        this.schedule = schedule;
        this.location = location;
    }
}