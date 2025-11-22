package com.alaya.coursesystem.alaya_course_selection.common;

import com.alaya.coursesystem.alaya_course_selection.entity.Grade;
import lombok.Data;

import java.util.List;

@Data
public class Result<T> {
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode("success");
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode("error");
        result.setMessage(message);
        return result;
    }
    // 构造方法、getter/setter、静态工具方法（如success()）
}