package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/24 15:46
 */
@Data
public class FactFlashSaleAppointment {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

    /**
     * 预约人数
     */
    private Integer appointmentCount;

    /**
     * 取消预约人数
     */
    private Integer cancelAppointmentCount;

}
