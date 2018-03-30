package com.yyy.TourWeb.tools;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * Json转换工具类
 *
 * @author 长风
 */
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
    }

    private JsonUtils() {
    }

    /**
     * 字符串转对象
     *
     * @param jsonString json字符串
     * @param className  对象类型
     * @param <T>        TYPE
     * @return 转换后的对象
     */
    public static <T> T toObject(String jsonString, Class<T> className) {
        if (null != jsonString && !jsonString.isEmpty()) {
            try {
                return mapper.readValue(jsonString, className);
            } catch (Exception e) {
                log.error("[Json转换] 转换失败：" + e.getMessage());
            }
        } else {
            log.warn("[Json转换] 入参为空");
        }
        return null;
    }

    /**
     * 对象转json字符串
     *
     * @param object 对象
     * @return json字符串
     */
    public static String toString(Object object) {
        if (null != object) {
            try {
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                log.error("[Json转换] 转换失败：" + e.getMessage());
            }
        }
        return null;
    }

    public static JsonNode toJsonNode(String jsonString) {
        if (null != jsonString && !jsonString.isEmpty()) {
            try {
                return mapper.readTree(jsonString);
            } catch (Exception e) {
                log.error("[Json转换] 转换失败：" + e.getMessage());
            }
        } else {
            log.warn("[Json转换] 入参为空");
        }
        return null;
    }

    /**
     * 字符串转集合对象
     *
     * @param jsonString      json字符串
     * @param collectionClass 集合类型
     * @param className       对象类型
     * @param <T>             对象TYPE
     * @return 转换结果
     */
    public static <T> Object toCollection(String jsonString, Class<?> collectionClass, Class<T> className) {
        if (null != jsonString && !jsonString.isEmpty()) {
            try {
                JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, className);
                return mapper.readValue(jsonString, javaType);
            } catch (Exception e) {
                log.error("[Json转换] 转换失败：" + e.getMessage());
            }
        } else {
            log.warn("[Json转换] 入参为空");
        }
        return null;
    }
}
