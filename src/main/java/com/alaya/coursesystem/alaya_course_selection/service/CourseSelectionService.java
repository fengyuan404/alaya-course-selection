package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.GradeRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.dto.PageRequestDTO;
import com.alaya.coursesystem.alaya_course_selection.vo.PageResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * 选课核心服务（迭代3完整版，含容量校验、冲突检测、退课功能）
 */
@Service
@RequiredArgsConstructor
public class CourseSelectionService {

    // 注入依赖
    private final CourseSelectionRepository selectionRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;

    /**
     * 学生选课（核心方法，含完整校验）
     * @param courseId 课程ID
     * @param auth 登录用户信息
     * @return 选课记录
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"studentGrades", "courseGrades", "studentSchedule"}, allEntries = true)
    public CourseSelection selectCourse(Long courseId, Authentication auth) {
        // 1. 获取当前登录学生
        User student = (User) auth.getPrincipal();
        if (student == null) {
            throw new UnifiedExceptionHandler.BusinessException("未获取到登录用户信息");
        }

        // 2. 查询课程并校验存在性
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在：ID=" + courseId));



        // 3. 课程容量校验（迭代3核心要求）
        long selectedCount = selectionRepository.countByCourseAndStatus(course, "SELECTED");
        if (selectedCount >= course.getCapacity()) {
            throw new UnifiedExceptionHandler.BusinessException(
                    String.format("课程《%s》已满员（总容量：%d，已选：%d），无法选课",
                            course.getName(), course.getCapacity(), selectedCount)
            );
        }

        // 4. 校验是否重复选课


        Optional<CourseSelection> existingSelection = selectionRepository.findByUserAndCourse(student, course);
        if (existingSelection.isPresent()) {
            // 区分“已选未退”和“已退课可重选”
            if ("SELECTED".equals(existingSelection.get().getStatus())) {
                throw new UnifiedExceptionHandler.BusinessException("已选课程《" + course.getName() + "》，无需重复选择");
            } else {
                // 已退课的情况：更新状态为已选，无需新增记录
                CourseSelection selection = existingSelection.get();
                selection.setStatus("SELECTED");
                selection.setSelectTime(LocalDateTime.now());
                // 兜底：若原有记录semester为空，补充赋值
                if (selection.getSemester() == null || selection.getSemester().isEmpty()) {
                    selection.setSemester(course.getSemester());
                }
                return selectionRepository.save(selection);
            }
           // throw new UnifiedExceptionHandler.BusinessException("已选课程《" + course.getName() + "》，无需重复选择");
        }


        // 5. 时间冲突检测（迭代3核心要求）
        List<CourseSelection> myActiveCourses = selectionRepository.findByUserAndStatus(student, "SELECTED");
        for (CourseSelection cs : myActiveCourses) {
            String existingSchedule = cs.getCourse().getSchedule();
            String newSchedule = course.getSchedule();
            if (isTimeConflict(existingSchedule, newSchedule)) {
                throw new UnifiedExceptionHandler.BusinessException(
                        String.format("时间冲突：已选课程《%s》（%s）与当前课程《%s》（%s）时间段重叠",
                                cs.getCourse().getName(), existingSchedule,
                                course.getName(), newSchedule)
                );
            }
        }

        // 6. 新增选课记录
        CourseSelection newSelection = new CourseSelection();
        newSelection.setUser(student);
        newSelection.setCourse(course);
        newSelection.setStatus("SELECTED");
        newSelection.setSelectTime(LocalDateTime.now());
        // ========== 新增：补充 semester 字段赋值（核心修复） ==========
// 方案1：从课程表获取学期（推荐，课程归属的学期更合理）
        newSelection.setSemester(course.getSemester());
// 方案2：若课程表无semester字段，可固定当前学期（临时方案）
// newSelection.setSemester("2025-2026-1");
        return selectionRepository.save(newSelection);
    }

    /**
     * 学生退课（释放课程容量）
     * @param selectionId 选课记录ID
     * @param auth 登录用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"studentGrades", "courseGrades", "studentSchedule"}, allEntries = true)
    public void withdrawCourse(Long selectionId, Authentication auth) {
        User student = (User) auth.getPrincipal();

        // 1. 查询选课记录并校验存在性
        CourseSelection selection = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("选课记录不存在：ID=" + selectionId));

        // 2. 校验选课归属权（只能退自己的课）
        if (!selection.getUser().getId().equals(student.getId())) {
            throw new UnifiedExceptionHandler.BusinessException("无权退他人的选课记录");
        }

        // 3. 校验课程状态（只能退已选状态的课）
        if (!"SELECTED".equals(selection.getStatus())) {
            throw new UnifiedExceptionHandler.BusinessException("课程已退课，无需重复操作");
        }

        // 4. 更新状态为已退课（保留记录，便于统计）
        selection.setStatus("WITHDRAWN");
        selectionRepository.save(selection);

        // 若需物理删除（简化版），替换为：
        // selectionRepository.delete(selection);
    }

    /**
     * 查询学生个人课表（按时间排序）
     * @param auth 登录用户信息
     * @return 排序后的已选课程列表
     */
//    @Cacheable(value = "studentSchedule", key = "#auth.principal.id")
//    public List<CourseSelection> getMySchedule(Authentication auth) {
//        User student = (User) auth.getPrincipal();
//        List<CourseSelection> myCourses = selectionRepository.findByUserAndStatus(student, "SELECTED");
//
//        // 按上课时间排序（周一→周日，早→晚）
//        myCourses.sort(Comparator.comparing(cs -> {
//            String schedule = cs.getCourse().getSchedule();
//            // 解析星期和节次（例："周一 1-2节" → 排序值=101）
//            String[] parts = schedule.split(" ");
//            int weekOrder = getWeekOrder(parts[0]);
//            int classOrder = Integer.parseInt(parts[1].split("-")[0]);
//            return weekOrder * 100 + classOrder;
//        }));
//
//        return myCourses;
//    }


    @Cacheable(value = "studentSchedule", key = "#auth.principal.id")
    public List<CourseSelection> getMySchedule(Authentication auth) {
        User student = (User) auth.getPrincipal();
        List<CourseSelection> myCourses = selectionRepository.findByUserAndStatus(student, "SELECTED");

        // 按上课时间排序（添加容错处理）
        myCourses.sort(Comparator.comparing(cs -> {
            String schedule = cs.getCourse().getSchedule();
            if (schedule == null || schedule.trim().isEmpty()) {
                // 空时间默认排最后（星期0，节次0）
                return 0;
            }
            String[] parts = schedule.split(" ");
            // 处理拆分后数组长度不足2的情况
            String weekPart = parts.length >= 1 ? parts[0] : "";
            String sectionPart = parts.length >= 2 ? parts[1] : "0-0节";

            int weekOrder = getWeekOrder(weekPart); // 星期解析容错（无效星期返回0）
            int[] sections = parseSections(sectionPart); // 节次解析已在parseSections中做容错
            return weekOrder * 100 + sections[0];
        }));

        return myCourses;
    }

    /**
     * 时间冲突判断（精准版）
     * @param existingSchedule 已选课程时间（例："周一 1-2节"）
     * @param newSchedule 待选课程时间（例："周一 2-3节"）
     * @return 是否冲突
     */
    private boolean isTimeConflict(String existingSchedule, String newSchedule) {
        // 分割星期和节次
        String[] existingParts = existingSchedule.split(" ");
        String[] newParts = newSchedule.split(" ");

        // 星期不同则不冲突
        if (!existingParts[0].equals(newParts[0])) {
            return false;
        }

        // 解析节次范围（例："1-2节" → [1,2]）
        int[] existingRange = parseSections(existingParts[1]);
        int[] newRange = parseSections(newParts[1]);

        // 区间重叠则冲突（包含首尾重叠）
        return !(existingRange[1] < newRange[0] || newRange[1] < existingRange[0]);
    }

    /**
     * 解析节次字符串为数字范围
     * @param sectionStr 节次字符串（例："1-2节"）
     * @return 节次范围数组（例：[1,2]）
     */
//    private int[] parseSections(String sectionStr) {
//        try {
//            String numPart = sectionStr.replace("节", "").trim();
//            String[] nums = numPart.split("-");
//            return new int[]{
//                    Integer.parseInt(nums[0]),
//                    Integer.parseInt(nums[1])
//            };
//        } catch (Exception e) {
//            throw new UnifiedExceptionHandler.BusinessException("课程时间格式错误：" + sectionStr);
//        }
//    }


    private int[] parseSections(String sectionStr) {
        try {
            String numPart = sectionStr.replace("节", "").trim();
            String[] nums = numPart.split("-");
            // 处理拆分后长度不足2的情况
            int start = nums.length >= 1 ? Integer.parseInt(nums[0]) : 0;
            int end = nums.length >= 2 ? Integer.parseInt(nums[1]) : 0;
            return new int[]{start, end};
        } catch (Exception e) {
            // 格式错误默认节次为0-0
            return new int[]{0, 0};
        }
    }

    /**
     * 星期转换为排序序号（周一=1，周二=2...周日=7）
     * @param week 星期字符串（例："周一"）
     * @return 排序序号
     */
    private int getWeekOrder(String week) {
        return switch (week) {
            case "周一" -> 1;
            case "周二" -> 2;
            case "周三" -> 3;
            case "周四" -> 4;
            case "周五" -> 5;
            case "周六" -> 6;
            case "周日" -> 7;
            default -> 0;
        };
    }

    // 可选：查询学生所有选课记录（含已退课）
    public List<CourseSelection> getMyAllSelections(Authentication auth) {
        User student = (User) auth.getPrincipal();
        return selectionRepository.findByUser(student);
    }

    // 可选：校验课程是否还有剩余容量
    /**
     * 分页查询课程选课学生（教师用，含成绩状态）
     */
    public PageResponseVO<CourseSelection> getCourseStudentsPage(Long courseId, String keyword, PageRequestDTO pageRequest) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在：" + courseId));

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Page<CourseSelection> selectionPage;

        if (keyword != null && !keyword.isEmpty()) {
            selectionPage = selectionRepository.findByCourseIdAndKeyword(courseId, keyword, pageRequest.toPageable(sort));
        } else {
            selectionPage = selectionRepository.findByCourse_Id(courseId, pageRequest.toPageable(sort));
        }

        // 填充成绩状态和成绩信息
        for (CourseSelection cs : selectionPage.getContent()) {
            gradeRepository.findBySelectionId(cs.getId()).ifPresentOrElse(
                grade -> {
                    cs.setGradeStatus("已录入");
                    Map<String, Object> gradeMap = new HashMap<>();
                    gradeMap.put("score", grade.getScore());
                    gradeMap.put("level", grade.getLevel());
                    gradeMap.put("comment", grade.getComment());
                    cs.setGrade(gradeMap);
                },
                () -> {
                    cs.setGradeStatus("未录入");
                    cs.setGrade(null);
                }
            );
        }

        return PageResponseVO.from(selectionPage);
    }

    public boolean hasRemainingCapacity(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在"));
        long selectedCount = selectionRepository.countByCourseAndStatus(course, "SELECTED");
        return selectedCount < course.getCapacity();
    }
}