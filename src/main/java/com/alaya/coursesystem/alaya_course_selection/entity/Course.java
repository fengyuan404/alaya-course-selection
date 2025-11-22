package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @NotNull(message = "课程容量不能为空")
    @Min(value = 1, message = "课程容量不能小于1")
    private Integer capacity;

    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分不能小于1")
    private Integer credits;

    private String description;

    @NotBlank(message = "上课时间不能为空")
    private String schedule;

    @NotBlank(message = "上课地点不能为空")
    private String location;

    // 带参构造器（已存在）
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