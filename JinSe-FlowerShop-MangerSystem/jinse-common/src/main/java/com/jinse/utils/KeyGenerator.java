package com.jinse.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class KeyGenerator {


    /**
     * 生成 Redis Key（简洁模式：只拼接数值字段值，按字段名排序后拼接）
     * @param prefix 前缀（如 "activity:"）
     * @param queryParams 包含查询参数的对象
     * @return 前缀 + 值:值...（如 "activity:sale:page:1:1:10"）
     */
    public static <T> String generateKey(String prefix, T queryParams) {
        try {
            Class<?> clazz = queryParams.getClass();
            List<String> paramNames = new ArrayList<>();

            // 先收集所有字段名
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(queryParams);
                if (value != null) {
                    paramNames.add(field.getName());
                }
            }

            // 按字段名排序，保证同样参数生成的 Key 一致
            paramNames.sort(String::compareTo);

            // 按排序后的字段名顺序收集值
            List<String> paramValues = new ArrayList<>();
            for (String paramName : paramNames) {
                Field field = clazz.getDeclaredField(paramName);
                field.setAccessible(true);
                Object value = field.get(queryParams);
                paramValues.add(value.toString());
            }

            String key = prefix + paramValues.stream().collect(Collectors.joining(":"));
            return key;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("生成Key失败", e);
        }
    }

    /**
     * 生成 Redis Key（直接拼接字符串列表）
     * @param parts Key 的各个组成部分（如 "activity:", "1", "sale:", "1"）
     * @return 拼接后的 Key（如 "activity:1:sale:1"）
     */
    public static String generateKey(String... parts) {
        return Arrays.stream(parts).collect(Collectors.joining());
    }
}
