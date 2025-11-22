// 包路径：com.alaya.coursesystem.alaya_course_selection.entity
package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_selections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})) // 防止重复选课
@Data
@NoArgsConstructor
@AllArgsConstructor
//选课记录实体
public class CourseSelection {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // 多对一：多个选课记录对应一个用户
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String semester; // 格式示例："2024-2025-1"（2024-2025学年第一学期）

// 补充getter/setter

    @ManyToOne // 多对一：多个选课记录对应一个课程
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    private String status; // 状态：SELECTED（已选）/WITHDRAWN（已退课）
    private LocalDateTime selectTime; // 选课时间
    @CreationTimestamp
    private LocalDateTime selectedAt; // 选课时间

}