package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;

// 课程实体类
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 课程名称

    @Column(name = "teacher_name")
    private String teacherName; // 授课教师

    private Integer capacity; // 课程容量

    // 构造方法、getter、setter
    public Course() {}

    public Course(String name, String teacherName, Integer capacity) {
        this.name = name;
        this.teacherName = teacherName;
        this.capacity = capacity;
    }

    // getter和setter省略（需手动生成）

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}