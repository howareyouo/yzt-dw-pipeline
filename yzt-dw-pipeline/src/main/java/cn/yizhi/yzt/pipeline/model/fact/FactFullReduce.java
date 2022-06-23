package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * @author aorui created on 2020/11/4
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactFullReduce {


        /**
         *
         */
        private String rowDate;

        /**
         * 店铺Id
         */
        private Integer shopId;

        /**
         * 满减活动id
         */
        private Integer promotionId;


        /**
         * 当日总订单数
         */
        private Integer orderCount;

        /**
         * 付款总额
         */
        private BigDecimal payTotal;

        /**
         * 当日付款人数
         */
        private Integer payNumber;


}
