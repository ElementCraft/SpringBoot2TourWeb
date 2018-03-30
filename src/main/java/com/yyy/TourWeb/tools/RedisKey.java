package com.yyy.TourWeb.tools;

/**
 * Redis生成key用
 *
 * @author yyy
 */
public class RedisKey {
    public static final String USER = "User:%s";

    public static String of(String key, Object... args){
        return String.format(key, args);
    }
}
