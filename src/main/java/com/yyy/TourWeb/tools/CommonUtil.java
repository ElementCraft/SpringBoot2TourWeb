package com.yyy.TourWeb.tools;

import java.util.Random;

/**
 * 常用工具类
 *
 * @author noobug.com
 */
public class CommonUtil {

    /**
     * 生成随机字符串
     *
     * @param len 长度
     * @return 指定长度的随机字符串
     */
    public static String randomString(int len){
        final String cache = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int size = cache.length();
        Random random = new Random();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(cache.charAt(random.nextInt(size)));
        }

        return result.toString();
    }
}
