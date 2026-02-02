package com.community.communitybackend.common.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; //状态码（200成功，500失败，404 没有这个接口）
    private String message; //提示信息(返回成功/返回错误/数据库查询失败)
    private T data;

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }
    public static <T> Result<T> success(Integer code,String message, T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error(String message){
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
    public static <T> Result<T> error(Integer code, String message){
        Result<T> result = new Result<>();
        result.setCode(code);//201
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message, T data){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }




}
