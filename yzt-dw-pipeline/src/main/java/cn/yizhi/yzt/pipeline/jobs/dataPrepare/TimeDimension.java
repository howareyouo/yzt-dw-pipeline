package cn.yizhi.yzt.pipeline.jobs.dataPrepare;


import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.types.Row;

import java.lang.reflect.Field;

@Getter
@Setter
public class TimeDimension {
    private int id;
    private String day;

    private int dayInMonth;
    private int dayInYear;
    private String month;
    private int monthInYear;
    private int year;
    private String halfOfYear;
    private String quarter;
    // Chinese:
    private String weekday;
    // English
    private String weekdayInEng;
    private int week;

    public TimeDimension(int id, String day, int dayInMonth, int dayInYear,
                         String month, int monthInYear, int year, String halfOfYear,
                         String quarter, String weekday, String weekdayInEng, int week) {
        this.id = id;
        this.day = day;
        this.dayInMonth = dayInMonth;
        this.dayInYear = dayInYear;
        this.month = month;
        this.monthInYear = monthInYear;
        this.year = year;
        this.halfOfYear = halfOfYear;
        this.quarter = quarter;
        this.weekday = weekday;
        this.weekdayInEng = weekdayInEng;
        this.week = week;
    }

    public Row toRow() {
        Row row = new Row(12);
        row.setField(0, this.id);
        row.setField(1, this.day);
        row.setField(2, this.dayInMonth);
        row.setField(3, this.dayInYear);
        row.setField(4, this.month);
        row.setField(5, this.monthInYear);
        row.setField(6, this.year);
        row.setField(7, this.halfOfYear);
        row.setField(8, this.quarter);
        row.setField(9, this.weekday);
        row.setField(10, this.weekdayInEng);
        row.setField(11, this.week);
        return row;
    }
}
