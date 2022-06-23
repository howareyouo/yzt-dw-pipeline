package cn.yizhi.yzt.pipeline.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author hucheng
 * @date 2020/10/30 14:20
 */
public class TimeUtil {

    private static final ThreadLocal<DateFormat> df_yyMMdd = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocal<DateFormat> df_yyMM = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM"));
    private static final ThreadLocal<DateFormat> df_yyMMddHH = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH"));
    private static final ThreadLocal<DateFormat> df_yyyyMMddHHmmss = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static final DateTimeFormatter yearMonthDayfmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter yearMonthfmt = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");


    /**
     * 时间转化 自定义
     */
    public static String convertTimeStampToDefinedTime(Timestamp timestamp, String format) {
        return new SimpleDateFormat(format).format(timestamp);
    }

    public static Long convertStringHourToLong(String time) throws Exception {
        return df_yyMMddHH.get().parse(time).getTime() / 1000;
    }

    public static Long convertStringDayToLong(String time) throws Exception {
        return df_yyMMdd.get().parse(time).getTime() / 1000;
    }

    public static Long convertStringMonthToLong(String time) throws Exception {
        return df_yyMM.get().parse(time).getTime() / 1000;
    }

    /**
     * 时间转化 天
     */
    public static String convertTimeStampToDay(Timestamp timestamp) {
        return df_yyMMdd.get().format(timestamp);
    }


    /**
     * 时间转化 小时
     */
    public static String convertTimeStampToHour(Timestamp timestamp) {
        return df_yyMMddHH.get().format(timestamp);
    }

    /**
     * 时间转化 秒
     */
    public static String convertTimeStampToSecond(Timestamp timestamp) {
        return df_yyyyMMddHHmmss.get().format(timestamp);
    }

    /**
     * 时间转化 秒
     */
    public static Date toUtilDate(String timestamp) throws Exception {
        return df_yyyyMMddHHmmss.get().parse(timestamp);
    }

    /**
     * 时间转化 月
     */
    public static String convertTimeStampToMonth(Timestamp timestamp) {
        return df_yyMM.get().format(timestamp);
    }

    /**
     * 计算至 `1970 00:00:00.000 GMT` 的天数
     */
    public static long toDays(java.sql.Date value) {
        return TimeUnit.MILLISECONDS.toDays(value.getTime());
    }


    public static long toDays(java.sql.Timestamp value) {
        return TimeUnit.MILLISECONDS.toDays(value.getTime());
    }


}
