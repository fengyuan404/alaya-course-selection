package com.alaya.coursesystem.alaya_course_selection.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeBatchDTO {
    private Long courseId;
    private Long studentId;
    private BigDecimal score;
    private String level;
    private String comment;
}