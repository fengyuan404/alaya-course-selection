package com.alaya.coursesystem.alaya_course_selection.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求DTO
 */
@Data
public class PageRequestDTO {
    // 1. 改为包装类型Integer，允许接收空值（空则用默认值）
    //(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    //@Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;

    // 2. 新增前端传的keyword/credits字段（匹配请求参数）
    private String keyword; // 对应前端的keyword=
    private Integer credits; // 对应前端的credits=（Integer允许空）

    // 3. 优化toPageable，处理空值（避免NPE）
    public org.springframework.data.domain.Pageable toPageable() {
        // 容错：空值则用默认值
        int finalPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int finalPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        return org.springframework.data.domain.PageRequest.of(
                finalPageNum - 1,
                finalPageSize,
                org.springframework.data.domain.Sort.by("id").descending()
        );
    }

    // 支持自定义排序（同理容错）
    public org.springframework.data.domain.Pageable toPageable(org.springframework.data.domain.Sort sort) {
        int finalPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int finalPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        return org.springframework.data.domain.PageRequest.of(
                finalPageNum - 1,
                finalPageSize,
                sort
        );
    }
}