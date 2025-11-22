package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "grade")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "selection_id", nullable = false, unique = true)
    private CourseSelection selection; // 关联选课记录

    private BigDecimal score; // 分数

    @Enumerated(EnumType.STRING)
    private GradeLevel level; // 等级

    private String comment; // 评语

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间

    // 等级枚举
    public enum GradeLevel {
        A, B, C, D, F
    }

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}