// 路径：src/main/java/com/alaya/coursesystem/alaya_course_selection/vo/PageResponseVO.java
package com.alaya.coursesystem.alaya_course_selection.vo;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 通用分页响应VO
 */
@Data
public class PageResponseVO<T> {
    private long total; // 总条数
    private int pages; // 总页数
    private int pageNum; // 当前页码（1基）
    private int pageSize; // 每页条数
    private List<T> list; // 数据列表

    // 从JPA Page转换
    public static <T> PageResponseVO<T> from(Page<T> page) {
        PageResponseVO<T> vo = new PageResponseVO<>();
        vo.setTotal(page.getTotalElements());
        vo.setPages(page.getTotalPages());
        vo.setPageNum(page.getNumber() + 1); // 转换为1基
        vo.setPageSize(page.getSize());
        vo.setList(page.getContent());
        return vo;
    }
}