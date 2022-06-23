package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import org.apache.flink.types.Row;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Consumer;


public class DateIterator implements Iterator<Row>, Serializable {
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDate;

    public DateIterator(String startDateStr, String endDateStr) {
        this.startDate = LocalDate.parse(startDateStr);
        this.endDate = LocalDate.parse(endDateStr);
        this.nextDate = this.startDate;
    }

    private TimeDimension createTimeDimensionData() {
        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateCompactPattern = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter monthPattern = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter monthOnlyPattern = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter yearPattern = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter weekPattern = DateTimeFormatter.ofPattern("w");


        //for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
        String querter = "";
        switch (this.nextDate.get(IsoFields.QUARTER_OF_YEAR)) {
            case 1:
                querter = "一季度";
                break;
            case 2:
                querter = "二季度";
                break;
            case 3:
                querter = "三季度";
                break;
            case 4:
                querter = "四季度";
                break;
            default:
                throw new RuntimeException("invalid quarter");
        }

        TimeDimension day = new TimeDimension(
                Integer.parseInt(nextDate.format(dateCompactPattern)),
                nextDate.format(datePattern),
                nextDate.getDayOfMonth(),
                nextDate.getDayOfYear(),
                nextDate.format(monthPattern),
                nextDate.getMonthValue(),
                nextDate.getYear(),
                nextDate.getMonthValue() <= 6 ? "上半年" : "下半年",
                querter,
                nextDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.SIMPLIFIED_CHINESE),
                nextDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                Integer.parseInt(nextDate.format(weekPattern))
        );

        nextDate = nextDate.plusDays(1L);

        return day;

    }

    @Override
    public boolean hasNext() {
        return this.nextDate.isBefore(this.endDate);
    }

    @Override
    public Row next() {
        return createTimeDimensionData().toRow();
    }

    @Override
    public void remove() {
        throw new RuntimeException("Not supported method");
    }

    @Override
    public void forEachRemaining(Consumer<? super Row> consumer) {
        while(hasNext()){
            consumer.accept(next());
        }
    }
}
