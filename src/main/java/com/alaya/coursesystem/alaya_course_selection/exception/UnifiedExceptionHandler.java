// 路径：src/main/java/com/alaya/coursesystem/alaya_course_selection/exception/UnifiedExceptionHandler.java
package com.alaya.coursesystem.alaya_course_selection.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 统一异常处理器（合并原两个处理器，避免重复处理）
 */
@Slf4j
@RestControllerAdvice
public class UnifiedExceptionHandler {

    // 通用业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return ResponseEntity.ok(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // 参数校验异常（@Valid）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常：", e);
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity.ok(ApiResponse.fail(400, "参数校验失败", errors));
    }

    // JSR303参数校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数约束异常：", e);
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(path, message);
        }
        return ResponseEntity.ok(ApiResponse.fail(400, "参数约束失败", errors));
    }

    // 权限不足异常
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足：", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(403, "权限不足，无法执行该操作"));
    }

    // 用户不存在异常
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("用户不存在：", e);
        return ResponseEntity.ok(ApiResponse.fail(404, e.getMessage()));
    }

    // 404异常
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("接口不存在：", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(404, "请求接口不存在"));
    }

    // 通用异常兜底
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("系统异常：", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "系统内部错误，请联系管理员"));
    }

    // 统一响应体（替换原有ApiResponse/ErrorResponse）
    @Data
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        // 1. 必须：无参构造函数（Jackson反序列化必需）
        public ApiResponse() {}

        // 2. 显式全参构造函数（工具方法调用）
        public ApiResponse(int code, String message, T data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        // 成功响应（无需修改，泛型保留完整）
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(200, "操作成功", data);
        }

        // 失败响应（无需修改）
        public static <T> ApiResponse<T> fail(int code, String message) {
            return new ApiResponse<>(code, message, null);
        }

        // 带数据的失败响应（无需修改）
        public static <T> ApiResponse<T> fail(int code, String message, T data) {
            return new ApiResponse<>(code, message, data);
        }


        // 构造器+getter/setter
//        public ApiResponse() {}
//        public ApiResponse(int code, String message, T data) {
//            this.code = code;
//            this.message = message;
//            this.data = data;
//        }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    // 业务异常基类（替换原有自定义异常）
    public static class BusinessException extends RuntimeException {
        private int code;

        public BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        public BusinessException(String message) {
            this(400, message);
        }

        public int getCode() { return code; }
    }
}