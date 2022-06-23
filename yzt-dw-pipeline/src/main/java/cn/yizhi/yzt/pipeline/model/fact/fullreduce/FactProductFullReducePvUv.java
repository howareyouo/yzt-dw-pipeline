package cn.yizhi.yzt.pipeline.model.fact.fullreduce;


import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/6
 */
@Getter
@Setter
public class FactProductFullReducePvUv {

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
    private Integer fullReduceId;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     *
     */
    private Integer pv;

    /**
     *
     */
    private Integer uv;


    /**
     * 主页分享次数
     */
    private Integer shareCount;

    @Override
    public String toString() {
        return "FactProductFullReducePvUv{" +
                "rowDate='" + rowDate + '\'' +
                ", shopId=" + shopId +
                ", fullReduceId=" + fullReduceId +
                ", productId=" + productId +
                ", pv=" + pv +
                ", uv=" + uv +
                ", shareCount=" + shareCount +
                '}';
    }
}
