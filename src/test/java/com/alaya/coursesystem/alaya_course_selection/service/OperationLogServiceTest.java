package com.alaya.coursesystem.alaya_course_selection.service;

import com.alaya.coursesystem.alaya_course_selection.entity.OperationLog;
import com.alaya.coursesystem.alaya_course_selection.repository.OperationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 操作日志记录测试
 */
@SpringBootTest
@Transactional
public class OperationLogServiceTest {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogRepository operationLogRepository;

    /**
     * 测试：日志记录功能
     */
    @Test
    void testRecordLog() {
        // 执行：记录日志
        String username = "test_log_user";
        String operationType = "成绩查询";
        String description = "测试日志记录";
        operationLogService.recordLog(username, operationType, description);

        // 验证：日志已保存
        OperationLog log = operationLogRepository.findAll().stream()
                .filter(l -> l.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        assertNotNull(log);
        assertEquals(operationType, log.getOperationType());
        assertEquals(description, log.getDescription());
        assertNotNull(log.getIpAddress()); // IP地址非空
        assertNotNull(log.getOperationTime()); // 操作时间非空
    }
}