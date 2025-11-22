// 路径：src/main/java/com/alaya/coursesystem/alaya_course_selection/dto/PageRequestDTO.java
package com.alaya.coursesystem.alaya_course_selection.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求DTO
 */
@Data
public class PageRequestDTO {
    @Min(value = 1, message = "页码不能小于1")
    private int pageNum = 1; // 前端传入1基页码

    @Min(value = 1, message = "每页条数不能小于1")
    private int pageSize = 10;

    // 转换为JPA的0基Pageable
    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(
                pageNum - 1, // 转换为0基索引
                pageSize,
                org.springframework.data.domain.Sort.by("id").descending() // 默认按ID降序
        );
    }

    // 支持自定义排序
    public org.springframework.data.domain.Pageable toPageable(org.springframework.data.domain.Sort sort) {
        return org.springframework.data.domain.PageRequest.of(
                pageNum - 1,
                pageSize,
                sort
        );
    }
}