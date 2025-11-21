package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "course")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程名称不能为空") // 非空校验
    @Size(max = 50, message = "课程名称长度不能超过50")
    @Column(nullable = false, unique = true)
    private String name;

    // 替换原teacherName，关联User实体（多对一：多个课程属于一个教师）
    @ManyToOne(fetch = FetchType.LAZY) // 懒加载优化性能
    @JoinColumn(name = "teacher_id", nullable = false) // 外键关联User表id
    private User teacher; // 授课教师（实体关联，而非字符串）

    @Min(value = 1, message = "课程容量不能小于1") // 最小值校验
    @Max(value = 200, message = "课程容量不能超过200")
    private Integer capacity; // 课程容量

    @Min(value = 1, message = "学分不能小于1")
    @Max(value = 8, message = "学分不能超过8")
    private Integer credits; // 学分

    @Size(max = 500, message = "课程描述长度不能超过500")
    private String description; // 课程描述

    @NotBlank(message = "上课时间不能为空")
    @Pattern(regexp = "^[周一至周日]{1,3} [0-9]{1,2}-[0-9]{1,2}节$",
            message = "上课时间格式错误（例：周一 1-2节）") // 时间格式标准化
    private String schedule; // 上课时间（标准化格式）

    @NotBlank(message = "上课地点不能为空")
    @Size(max = 50, message = "上课地点长度不能超过50")
    private String location; // 上课地点

    // 无参构造器
    public Course() {}

    // 带参构造器（适配关联实体）
    public Course(String name, User teacher, Integer capacity,
                  Integer credits, String description, String schedule, String location) {
        this.name = name;
        this.teacher = teacher;
        this.capacity = capacity;
        this.credits = credits;
        this.description = description;
        this.schedule = schedule;
        this.location = location;
    }
}