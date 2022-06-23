package cn.yizhi.yzt.pipeline.util;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.type.CollectionLikeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by exizhai on 4/14/2015.
 */
public class JsonMapper {
    private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

    private static JsonMapper defaultMapper;
    private static JsonMapper wrapRootMapper;
    private static JsonMapper nonEmptyMapper;

    private final ObjectMapper mapper;

    public JsonMapper() {
        this(null, false);
    }

    public JsonMapper(boolean unwrapRoot) {
        this(null, unwrapRoot);
    }

    public JsonMapper(JsonInclude.Include include, boolean wrapRoot) {
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置输出时包含属性的风格
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        if (wrapRoot) {
            mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        }
    }

    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
     */
    public synchronized static JsonMapper nonEmptyMapper() {
        if (nonEmptyMapper == null) {
            nonEmptyMapper = new JsonMapper(JsonInclude.Include.NON_EMPTY, false);
        }
        return nonEmptyMapper;
    }

    public synchronized static JsonMapper wrapRootMapper() {
        if (wrapRootMapper == null) {
            wrapRootMapper = new JsonMapper(true);
        }
        return wrapRootMapper;
    }

    /**
     * 创建默认Mapper
     */
    public synchronized static JsonMapper defaultMapper() {
        if (defaultMapper == null) {
            defaultMapper = new JsonMapper();
        }
        return defaultMapper;
    }

    /**
     * 对象转换成JSON字符串
     */
    public String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (IOException e) {
            logger.warn("Error when [toJson]: {}", o, e);
            return null;
        }
    }

    /**
     * 对象转换成JSON字符串
     */
    public String toPrettyJson(Object o) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException e) {
            logger.warn("Error when [toJson]: {}", o, e);
            return null;
        }
    }

    /**
     * JSON转换成Java对象
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.warn("Error when [fromJson]: {}", json, e);
            return null;
        }
    }

    /**
     * 把object转出clazz对象， 比如POJO和Map互换，字符串转换成Date
     *
     * @param object 原对象
     * @param clazz  目标类型
     */
    public <T> T convert(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        return mapper.convertValue(object, clazz);
    }

    /**
     * 如果json是数组格式，则挨个转换成clazz对象返回list，否则直接尝试转换成clazz对象返回list
     */
    public <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        try {
            JsonNode jsonNode = mapper.readTree(json);

            // 数组格式
            if (jsonNode.isArray()) {
                CollectionLikeType javaType = mapper.getTypeFactory().constructCollectionLikeType(ArrayList.class, clazz);
                return mapper.convertValue(jsonNode, javaType);
            }

            return Collections.singletonList(mapper.treeToValue(jsonNode, clazz));
        } catch (Exception e) {
            logger.warn("Error when [fromJsonArray]: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 抽取json中的数组属性到对象列表
     *
     * @param json         json 字符串
     * @param arrayPointer 数组属性路径, eg: /root/array, /obj/tar/items
     * @param clazz        对象类型
     */
    public <T> List<T> extractValues(String json, String arrayPointer, Class<T> clazz) {
        try {
            JsonNode jsonNode = mapper.readTree(json).at(arrayPointer);
            CollectionLikeType javaType = mapper.getTypeFactory().constructCollectionLikeType(ArrayList.class, clazz);
            return mapper.convertValue(jsonNode, javaType);
        } catch (IOException e) {
            logger.warn("Error when [extractValues]: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * 抽取json中的对象属性进行转换
     *
     * @param json         json 字符串
     * @param valuePointer 对象属性路径, eg: /root/obj, /obj/tar/item
     * @param clazz        对象类型
     */
    public <T> T extractValue(String json, String valuePointer, Class<T> clazz) {
        if (json != null) {
            try {
                JsonNode node = mapper.readTree(json).at(valuePointer);
                return mapper.treeToValue(node, clazz);
            } catch (IOException e) {
                logger.warn("Error when [extractValue]: {}", json, e);
            }
        }
        return null;
    }

    public JsonNode toJsonNode(String source) {
        if (source == null) {
            return null;
        }
        try {
            return mapper.readTree(source);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
