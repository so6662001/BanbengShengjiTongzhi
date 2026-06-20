package com.company.notify.service.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;

import java.util.List;

/** 轻量 JSON 工具，用于 channels / preNotifyDays / audience 条件等字段的序列化。 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtil() {}

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "JSON 序列化失败: " + e.getMessage());
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }

    public static <T> List<T> parseList(String json, Class<T> clazz) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }

    public static <T> T parse(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }
}
