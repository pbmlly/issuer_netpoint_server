package com.csnt.ins.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by hx 017/7/17.
 */
public class UtilJson {
    private static final Logger logger = LoggerFactory.getLogger(UtilJson.class);

    public static ObjectMapper getObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        return objectMapper;
    }

    public static String toJSONString(Object o) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("toJSONString error {}", e);
        }

        return null;
    }

    public static String toJson(Object o) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toJSONBytes(Object o) {
        return toJSONString(o).getBytes(Charset.forName("utf-8"));
    }

    public static Object toObject(String jsonStr, Class<?> c) {
        ObjectMapper objectMapper = getObjectMapper();

        try {
            return objectMapper.readValue(jsonStr, c);
        } catch (IOException e) {
            logger.error("toObject error {}", e);
        }

        return null;
    }

    public static Object toObject(byte[] jsonBytes, Class<?> c) {
        return toObject(new String(jsonBytes, Charset.forName("utf-8")), c);
    }

    public static Object toObject(File file, Class<?> c) {
        ObjectMapper objectMapper = getObjectMapper();

        try {
            return objectMapper.readValue(file, c);
        } catch (IOException e) {
            logger.error("toObject error {}", e);
        }
        return null;
    }

    public static <T> T toJsonObjectList(File file, TypeReference<T> typeReference) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            return (T) objectMapper.readValue(file, typeReference);
        } catch (JsonParseException e) {
            logger.error("decode(String, JsonTypeReference<T>)", e);
        } catch (JsonMappingException e) {
            logger.error("decode(String, JsonTypeReference<T>)", e);
        } catch (IOException e) {
            logger.error("decode(String, JsonTypeReference<T>)", e);
        }
        return null;
    }

    public static <T> T toJsonObject(String jsonStr, TypeReference<T> typeReference) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            return objectMapper.readValue(jsonStr, typeReference);
        } catch (IOException e) {
            logger.error("toObject error {}", e);
            throw new RuntimeException("decode jsonStr error", e);
        }
    }
}
