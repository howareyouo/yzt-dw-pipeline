package cn.yizhi.yzt.pipeline.jdbc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zjzhang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name();

    FieldType type() default FieldType.NORMAL;

    public static enum FieldType {
        // 是否是主键
        PK,
        // 是否是检索字段
        KEY,
        // 普通字段
        NORMAL;
    }
}
