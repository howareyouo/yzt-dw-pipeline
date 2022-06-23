package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/8/13 15:43
 */
@Data
public class FactOrderClean {
    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 会员id
     */
    private Integer memberId;

    /**
     *  直播间id
     */
    private Integer liveRoomId;

    /**
     * 订单id
     */
    private Integer id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 来源
     */
    private String source;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 姓名
     */
    private String name;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 更新时间
     */
    private Timestamp updatedAt;

    /**
     * flink 系统处理数据时间
     */
    private Timestamp proctime;
}
