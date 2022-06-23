package cn.yizhi.yzt.pipeline.model.fact.flashsale.product;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/11/2 15:18
 */
@Data
public class FactFlashSaleProductAppointment {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;


    /**
     * 秒杀活动id
     */
    private  Integer productId;

    /**
     * 预约人数
     */
    private Integer appointmentCount;

    /**
     * 取消预约人数
     */
    private Integer cancelAppointmentCount;
}
