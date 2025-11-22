package com.alaya.coursesystem.alaya_course_selection.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // 操作用户

    private String operationType; // 操作类型

    private String description; // 操作描述

    private String ipAddress; // IP地址

    private LocalDateTime operationTime; // 操作时间

    @PrePersist
    public void prePersist() {
        this.operationTime = LocalDateTime.now();
    }
}