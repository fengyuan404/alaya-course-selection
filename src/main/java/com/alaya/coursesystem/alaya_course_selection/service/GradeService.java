package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.Course;
import com.alaya.coursesystem.alaya_course_selection.entity.Grade;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import com.alaya.coursesystem.alaya_course_selection.dto.GradeBatchDTO;
import com.alaya.coursesystem.alaya_course_selection.entity.CourseSelection;
import com.alaya.coursesystem.alaya_course_selection.exception.UnifiedExceptionHandler;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.CourseSelectionRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.GradeRepository;
import com.alaya.coursesystem.alaya_course_selection.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final CourseSelectionRepository selectionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // 录入/修改成绩
    @Transactional
    public Grade saveGrade(Long selectionId, BigDecimal score, Grade.GradeLevel level, String comment, User teacher) {
        // 验证选课记录存在
        var selection = selectionRepository.findById(selectionId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("选课记录不存在"));

        // 验证教师权限
        if (!selection.getCourse().getTeacher().getId().equals(teacher.getId())) {
            throw new UnifiedExceptionHandler.BusinessException("无权操作其他教师的课程成绩");
        }

        // 验证分数范围
        if (score != null && (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal(100)) > 0)) {
            throw new UnifiedExceptionHandler.BusinessException("分数必须在0-100之间");
        }

        // 查找或创建成绩记录
        var grade = gradeRepository.findBySelectionId(selectionId).orElse(new Grade());
        grade.setSelection(selection);
        grade.setScore(score);
        grade.setLevel(level);
        grade.setComment(comment);

        return gradeRepository.save(grade);
    }

    // 学生查询个人成绩
    public List<Grade> getStudentGrades(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("学生不存在"));
        return gradeRepository.findBySelection_UserOrderBySelection_Course_Name(student);
    }

    // 教师查询课程成绩
    public List<Grade> getCourseGrades(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在"));

        // 验证权限
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new UnifiedExceptionHandler.BusinessException("无权查看其他教师的课程成绩");
        }

        return gradeRepository.findBySelection_Course(course);
    }

    // 成绩统计分析
    public Map<String, Object> analyzeCourseGrades(Long courseId, Long teacherId) {
        List<Grade> grades = getCourseGrades(courseId, teacherId);

        // 计算平均分
        BigDecimal avgScore = grades.stream()
                .filter(g -> g.getScore() != null)
                .map(Grade::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(grades.size()), 2, java.math.RoundingMode.HALF_UP);

        // 统计等级分布
        Map<Grade.GradeLevel, Long> levelDistribution = grades.stream()
                .filter(g -> g.getLevel() != null)
                .collect(Collectors.groupingBy(Grade::getLevel, Collectors.counting()));

        // 组装结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalStudents", grades.size());
        result.put("averageScore", avgScore);
        result.put("levelDistribution", levelDistribution);

        return result;
    }

    /**
     * 学生按学期查询成绩
     * @param studentId 学生ID
     * @param semester 学期（如"2024-2025-1"）
     * @return 该学期的成绩列表
     */
    public List<Grade> getStudentGradesBySemester(Long studentId, String semester) {
        // 1. 校验学生存在
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("学生不存在"));
        // 2. 关联选课记录的学期筛选
        return gradeRepository.findBySelection_User_IdAndSelection_Semester(studentId, semester);
    }

    /**
     * 教师按学期查询课程成绩
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param semester 学期
     * @return 该学期该课程的成绩列表
     */
    /**
     * 单条保存成绩（复用批量保存逻辑）
     */
    @Transactional
    public void saveGradeSingle(GradeBatchDTO dto, User teacher) {
        batchSaveGrades(java.util.Collections.singletonList(dto), teacher);
    }

    /**
     * 批量保存成绩（教师用）
     */
    @Transactional
    public void batchSaveGrades(List<GradeBatchDTO> gradeList, User teacher) {
        if (gradeList == null || gradeList.isEmpty()) {
            return;
        }
        for (GradeBatchDTO dto : gradeList) {
            // 查找课程
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在：" + dto.getCourseId()));
            // 权限校验：只能给自己课程的选课记录打分
            if (!course.getTeacher().getId().equals(teacher.getId())) {
                throw new UnifiedExceptionHandler.BusinessException("无权操作其他教师的课程成绩");
            }
            // 查找学生
            User student = userRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("学生不存在：" + dto.getStudentId()));
            // 查找选课记录
            CourseSelection selection = selectionRepository.findByUserAndCourse(student, course)
                    .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("选课记录不存在：学生ID=" + dto.getStudentId() + ", 课程ID=" + dto.getCourseId()));
            // 分数范围校验
            if (dto.getScore() != null && (dto.getScore().compareTo(java.math.BigDecimal.ZERO) < 0 || dto.getScore().compareTo(new java.math.BigDecimal(100)) > 0)) {
                throw new UnifiedExceptionHandler.BusinessException("分数必须在0-100之间");
            }
            // 查找或创建成绩
            Grade grade = gradeRepository.findBySelectionId(selection.getId()).orElse(new Grade());
            grade.setSelection(selection);
            grade.setScore(dto.getScore());
            // 转换level字符串为GradeLevel枚举
            if (dto.getLevel() != null && !dto.getLevel().isEmpty()) {
                try {
                    grade.setLevel(Grade.GradeLevel.valueOf(dto.getLevel()));
                } catch (IllegalArgumentException e) {
                    throw new UnifiedExceptionHandler.BusinessException("无效的成绩等级：" + dto.getLevel());
                }
            }
            grade.setComment(dto.getComment());
            gradeRepository.save(grade);
        }
    }

    public List<Grade> getCourseGradesBySemester(Long courseId, Long teacherId, String semester) {
        // 1. 权限校验（教师只能查自己的课程）
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new UnifiedExceptionHandler.BusinessException("课程不存在"));
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new UnifiedExceptionHandler.BusinessException("无权操作其他教师的课程");
        }
        // 2. 按课程+学期筛选
        return gradeRepository.findBySelection_Course_IdAndSelection_Semester(courseId, semester);
    }



}