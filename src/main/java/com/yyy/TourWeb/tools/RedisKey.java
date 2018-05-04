package com.yyy.TourWeb.tools;

/**
 * Redis生成key用
 *
 * @author yyy
 */
public class RedisKey {
    public static final String USER = "Users";
    public static final String ARTICLE = "Articles";
    public static final String USER_ARTICLE = "UserArticle:%s";
    public static final String COMMENT = "Comments:%s";

    public static String of(String key, Object... args) {
        return String.format(key, args);
    }
}
