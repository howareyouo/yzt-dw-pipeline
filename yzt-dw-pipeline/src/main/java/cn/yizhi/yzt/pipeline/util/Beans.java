package cn.yizhi.yzt.pipeline.util;

import java.lang.reflect.InvocationTargetException;

public class Beans {


    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        return copyProperties(source, targetClass, false);
    }

    public static <T> T copyProperties(Object source, Class<T> targetClass, boolean ignoreNull) {
        T result = null;
        try {
            result = targetClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static void copyProperties(Object source, Object target, boolean ignoreNull) {

    }
}
