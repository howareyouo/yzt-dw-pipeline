package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.common.EventType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.RawLogStream;
import cn.yizhi.yzt.pipeline.model.ods.CouponTemplate;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author hucheng
 * @date 2020/8/19 15:23
 */
public class OdsLogStreamJob extends StreamJob {
    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-index.yml");

        //注册mysql表
        this.createTableFromJdbc("ods_coupon_template", "ods_coupon_template", CouponTemplate.class);

        //接收流
        DataStream<RawLogStream> rawLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_SDK_LOG_STREAM, RawLogStream.class);

        DataStream<OdsLogStream> odsLogStreamDs = rawLogStreamDs.filter(new FilterFunction<RawLogStream>() {
            @Override
            public boolean filter(RawLogStream value) throws Exception {

                //初级清洗
                if (value.getShopId() == null || value.getShopId() <= 0
                        || value.getTaroEnv() == null || value.getEventName() == null
                        || value.getEventTime() == null) {
                    return  false;
                }

                //特殊事件过滤
                if (EventType.VIEW_SHOP_HEART_BEAT.getName().equals(value.getEventName())) {
                    return  false;
                }

                //范围值筛选
                if (value.getEventName() != null) {
                    if (value.getEventName().contains("Goods")) {
                        if (value.getGoodsId() == null || value.getGoodsId() <= 0) {
                            return  false;
                        }
                    }

                    if (value.getEventName().contains("Activity")) {
                        if (value.getActivityType() == null || value.getActivityId() == null ||value.getActivityId() <= 0) {
                            return  false;
                        }
                    }

                    if (value.getEventName().contains("LiveRoom")) {
                        if (value.getLiveRoomId() == null || value.getLiveRoomId() <= 0) {
                            return  false;
                        }
                    }

                    if (value.getEventName().contains("Coupon")) {
                        return value.getCouponTemplateId() != null && value.getCouponTemplateId() > 0 && value.getCouponType() != null;
                    }
                }

                return true;
            }
        }).map(new MapFunction<RawLogStream, OdsLogStream>() {
            @Override
            public OdsLogStream map(RawLogStream value) throws Exception {
                //数据转换
                return convertToOdsLogStream(value);
            }
        });

        //转为table
        streamToTable(OdsLogStream.class, odsLogStreamDs, true);

        //日志数据清洗
        Table logStreamTable = this.sqlQuery("odsLogStreamDimQuery");


        //写入数据到kafka
        this.appendToKafka(logStreamTable, OdsLogStream.class, "ods_log_stream");
    }

    private static OdsLogStream convertToOdsLogStream(RawLogStream rawLogStream) {
        //时间转换
        OdsLogStream odsLogStream  = new OdsLogStream();
        odsLogStream.setShopId(rawLogStream.getShopId());
        odsLogStream.setMainShopId(rawLogStream.getMainShopId());
        odsLogStream.setUserId(rawLogStream.getUserId());
        odsLogStream.setGoodsId(rawLogStream.getGoodsId());

        String channel;
        switch (rawLogStream.getTaroEnv()){
            case "weapp":
                channel = "wxapp";
                break;
            case "h5":
                channel = "web";
                break;
            default:
                channel = rawLogStream.getTaroEnv();
        }
        odsLogStream.setTaroEnv(channel);

        odsLogStream.setDeviceId(rawLogStream.getDeviceId());
        odsLogStream.setOpenId(rawLogStream.getOpenId());
        odsLogStream.setDeviceModel(rawLogStream.getDeviceModel());
        odsLogStream.setDeviceBrand(rawLogStream.getDeviceBrand());
        odsLogStream.setSystemName(rawLogStream.getSystemName());
        odsLogStream.setSystemVersion(rawLogStream.getSystemVersion());
        odsLogStream.setAppVersion(rawLogStream.getAppVersion());
        odsLogStream.setEventName(rawLogStream.getEventName());

        odsLogStream.setEventTime(convertToTimestamp(rawLogStream.getEventTime()));

        odsLogStream.setUrl(rawLogStream.getUrl());
        odsLogStream.setQuery(rawLogStream.getQuery());
        odsLogStream.setKeyword(rawLogStream.getKeyword());
        odsLogStream.setQuantity(rawLogStream.getQuantity());

        //优惠劵也转为活动
        if(rawLogStream.getCouponTemplateId() != null && rawLogStream.getCouponTemplateId() > 0){
            odsLogStream.setCouponTemplateId(rawLogStream.getCouponTemplateId());
            odsLogStream.setPromotionType(1);
            odsLogStream.setPromotionId(rawLogStream.getCouponTemplateId());
        }

        /**
         * 1 优惠劵  2 满减  3 秒杀  4 直播 5 抽奖 9 拼团（和业务保持一致）
         */
        if(rawLogStream.getActivityType() != null){
            odsLogStream.setPromotionId(rawLogStream.getActivityId());
            switch (rawLogStream.getActivityType()){
                case "fullReduce":
                    odsLogStream.setPromotionType(2);
                    break;
                case "flashSale":
                    odsLogStream.setPromotionType(3);
                    break;
                case "liveRoom":
                    odsLogStream.setPromotionType(4);
                    break;
                case "lottery":
                    odsLogStream.setPromotionType(5);
                    break;
                case "groupBuy":
                    odsLogStream.setPromotionType(9);
                    break;
                default:
                    odsLogStream.setPromotionType(0);
                    break;
            }
        }

        odsLogStream.setLiveRoomId(rawLogStream.getLiveRoomId());
        odsLogStream.setSource(rawLogStream.getSource());
        odsLogStream.setUserName(rawLogStream.getUserName());
        odsLogStream.setUserPhone(rawLogStream.getUserPhone());
        odsLogStream.setGoodsName(rawLogStream.getGoodsName());
        odsLogStream.setUuid(rawLogStream.getUuid());
        odsLogStream.setBeginTime(rawLogStream.getBeginTime());
        odsLogStream.setEndTime(rawLogStream.getEndTime());
        odsLogStream.setProductType(rawLogStream.getProductType());

        odsLogStream.setAvatar(rawLogStream.getAvatar());
        odsLogStream.setCouponType(rawLogStream.getCouponType());

        return odsLogStream;
    }

    public static  Timestamp convertToTimestamp(Long unixTime) {
        if (unixTime == null) {
            return null;
        }
        return Timestamp.from(Instant.ofEpochMilli(unixTime));
    }
}
