package com.alaya.coursesystem.alaya_course_selection.vo;

import lombok.Data;
import java.util.List;

@Data
public class CourseStatVO {
    private StatSummary summary;
    private List<CourseStatItem> list;
    private long total;

    @Data
    public static class StatSummary {
        private long totalCourse;
        private long totalSelected;
        private double averageSelected;
        private long hotCourseCount;
    }

    @Data
    public static class CourseStatItem {
        private String name;
        private String teacherName;
        private int credits;
        private int capacity;
        private long selectedCount;
        private double selectedRate;
        private String semester;
    }
}
