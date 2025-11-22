package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 选课核心服务（迭代3完整版，含容量校验、冲突检测、退课功能）
 */
@Service
@RequiredArgsConstructor
public class CourseSelectionService {

    // 注入依赖
    private final CourseSelectionRepository selectionRepository;
    private final CourseRepository courseRepository;

    /**
     * 学生选课（核心方法，含完整校验）
     * @param courseId 课程ID
     * @param auth 登录用户信息
     * @return 选课记录
     */
    @Transactional(rollbackFor = Exception.class)
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
//            if ("SELECTED".equals(existingSelection.get().getStatus())) {
//                throw new UnifiedExceptionHandler.BusinessException("已选课程《" + course.getName() + "》，无需重复选择");
//            } else {
//                // 已退课的情况：更新状态为已选，无需新增记录
//                CourseSelection selection = existingSelection.get();
//                selection.setStatus("SELECTED");
//                selection.setSelectTime(LocalDateTime.now());
//                return selectionRepository.save(selection);
//            }
            throw new UnifiedExceptionHandler.BusinessException("已选课程《" + course.getName() + "》，无需重复选择");
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
        return selectionRepository.save(newSelection);
    }

    /**
     * 学生退课（释放课程容量）
     * @param selectionId 选课记录ID
     * @param auth 登录用户信息
     */
    @Transactional(rollbackFor = Exception.class)
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
    public List<CourseSelection> getMySchedule(Authentication auth) {
        User student = (User) auth.getPrincipal();
        List<CourseSelection> myCourses = selectionRepository.findByUserAndStatus(student, "SELECTED");

        // 按上课时间排序（周一→周日，早→晚）
        myCourses.sort(Comparator.comparing(cs -> {
            String schedule = cs.getCourse().getSchedule();
            // 解析星期和节次（例："周一 1-2节" → 排序值=101）
            String[] parts = schedule.split(" ");
            int weekOrder = getWeekOrder(parts[0]);
            int classOrder = Integer.parseInt(parts[1].split("-")[0]);
            return weekOrder * 100 + classOrder;
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
    private int[] parseSections(String sectionStr) {
        try {
            String numPart = sectionStr.replace("节", "").trim();
            String[] nums = numPart.split("-");
            return new int[]{
                    Integer.parseInt(nums[0]),
                    Integer.parseInt(nums[1])
            };
        } catch (Exception e) {
            throw new UnifiedExceptionHandler.BusinessException("课程时间格式错误：" + sectionStr);
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
    public boolean hasRemainingCapacity(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在"));
        long selectedCount = selectionRepository.countByCourseAndStatus(course, "SELECTED");
        return selectedCount < course.getCapacity();
    }
}