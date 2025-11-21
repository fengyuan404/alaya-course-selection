package com.alaya.coursesystem.alaya_course_selection.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.HashMap;
import java.util.Map;

// 全局异常处理器，统一拦截所有控制器异常

@RestControllerAdvice("com.alaya.coursesystem.alaya_course_selection") // 指定扫描包（可选）
@org.springframework.stereotype.Component("alayaGlobalExceptionHandler") // 手动指定唯一Bean名称
public class GlobalExceptionHandler {

    // 处理业务异常（自定义）
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {
        ApiResponse response = new ApiResponse();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(e.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 处理参数校验异常（如Course实体的@NotBlank）
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(BindException e) {
        ApiResponse response = new ApiResponse();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        // 提取第一个校验错误信息
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        response.setMessage(message);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        ApiResponse response = new ApiResponse();
        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("系统异常：" + e.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 统一接口响应格式
    @Data
    public static class ApiResponse {
        private int code; // 响应码（200成功，400参数错误，500系统错误）
        private String message; // 响应信息
        private Object data; // 响应数据

        // 静态方法：快速构建成功响应
        public static ApiResponse success(Object data) {
            ApiResponse response = new ApiResponse();
            response.setCode(HttpStatus.OK.value());
            response.setMessage("操作成功");
            response.setData(data);
            return response;
        }
    }
}