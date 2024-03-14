package org.example.reggie.common;

/**
 * 基于ThreadLoacl封装工具类
 * 用于保存和获取当前登录用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId){
        threadLocal.set(userId);
    }

    public static Long getCurrentUserId(){
        return threadLocal.get();
    }
}
