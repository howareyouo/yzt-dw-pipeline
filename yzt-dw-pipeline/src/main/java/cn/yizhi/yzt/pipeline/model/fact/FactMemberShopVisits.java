package cn.yizhi.yzt.pipeline.model.fact;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author aorui created on 2021/1/5
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactMemberShopVisits {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("visits_start")
    private Timestamp visitsStart;

    @JsonProperty("visits_end")
    private Timestamp visitsEnd;

    @JsonProperty("visits_duration")
    private int visitsDuration;

    @JsonProperty("created_at")
    private String createdAt;


    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;


    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactMemberShopVisitsTimes {


        @JsonProperty("id")
        private Integer id;

        @JsonProperty("last_view_shop_time")
        private Timestamp lastViewShopTime;

    }


    public static FactMemberShopVisitsTimes from(FactMemberShopVisits o) {
        FactMemberShopVisitsTimes factMemberShopVisitsTimes = new FactMemberShopVisitsTimes();
        factMemberShopVisitsTimes.setId(o.getMemberId());
        factMemberShopVisitsTimes.setLastViewShopTime(o.getVisitsStart());
        if (o.getVisitsEnd() != null) {
            factMemberShopVisitsTimes.setLastViewShopTime(o.getVisitsEnd());
        }
        return factMemberShopVisitsTimes;

    }
}
