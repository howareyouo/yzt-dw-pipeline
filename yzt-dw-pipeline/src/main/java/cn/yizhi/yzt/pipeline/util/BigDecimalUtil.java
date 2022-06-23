package cn.yizhi.yzt.pipeline.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
    // 除法运算默认精度
    private static final int DEF_DIV_SCALE = 10;

    private BigDecimalUtil() {

    }

    /**
     * 精确加法
     */
    public static double add(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.add(b2).doubleValue();
    }


    /**
     * 精确减法
     */
    public static double sub(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.subtract(b2).doubleValue();
    }


    /**
     * 精确乘法
     */
    public static double mul(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.multiply(b2).doubleValue();
    }


    /**
     * 精确除法 使用默认精度
     */
    public static double div(double value1, double value2) {
        return div(value1, value2, DEF_DIV_SCALE);
    }


    /**
     * 精确除法
     *
     * @param scale 精度
     */
    public static double div(double value1, double value2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精确度不能小于0");
        }
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return (b1.divide(b2, scale, RoundingMode.UP)).doubleValue();
    }

    /**
     * 四舍五入
     *
     * @param scale 小数点后保留几位
     */
    public static double round(double v, int scale) {
        return div(v, 1, scale);
    }

    public static void main(String[] args) {
        double div = div(4262.01, 59347.40, 4);
        System.out.println(div);
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(div));
        System.out.println(bigDecimal.toString());

    }
}
