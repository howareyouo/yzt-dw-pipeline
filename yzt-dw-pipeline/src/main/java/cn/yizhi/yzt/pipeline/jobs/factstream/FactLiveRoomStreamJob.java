package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.liveroom.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.fact.liveroom.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.types.Row;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hucheng
 * @date 2020/8/10 14:42
 */
public class FactLiveRoomStreamJob extends StreamJob {

    private static final String jobDescription = "直播指标计算";

    @Override
    public String getJobDescription() {
        return jobDescription;
    }

    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-live-room.yml");

        //注册流
        DataStream<OdsLogStream> odsLogStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class);
        DataStream<OdsLiveRoomOrderItem> odsLiveRoomOrderItemDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_LIVE_ROOM_ORDER_ITEM, OdsLiveRoomOrderItem.class);

        //注册表
        this.streamToTable(OdsLogStream.class, odsLogStream, true);
        this.createTableFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_ITEM, OdsOrderItem.class, true);
        this.streamToTable(OdsLiveRoomOrderItem.class, odsLiveRoomOrderItemDataStream, true);

        //注册订单表
        this.createTableFromJdbc("ods_emall_order", "ods_emall_order", OdsEmallOrder.class);

        //已支付订单清洗
        tableEnv.createTemporaryView("emall_order_pay", this.sqlQuery("orderClean"));
        //订单商品清洗
        tableEnv.createTemporaryView("live_room_order_pay", this.sqlQuery("productOrderClean"));
        //新老客数据清洗
        tableEnv.createTemporaryView("new_member", this.sqlQuery("newMemberClean"));


        //累计观看人数和次数
        Table factLiveRoomPvAndUvTable = this.sqlQuery("factLiveRoomPvAndUv");
        //通过分享进入直播间
        Table factLiveRoomUvByShareTable = this.sqlQuery("factLiveRoomUvByShare");
        //领取优惠劵人数
        Table factLiveRoomReceivedCouponTable = this.sqlQuery("factLiveRoomReceivedCoupon");
        //直播间订单数据
        Table factLiveRoomOrderTable = this.sqlQuery("factLiveRoomOrder");
        //拓新客
        Table factLiveRoomNewNumberTable = this.sqlQuery("factLiveRoomNewNumber");
        //商品销售数据
        Table factLiveRoomProductOrderTable = this.sqlQuery("factLiveRoomProductOrder");


        DataStream<FactLiveRoomUvAndPv> factLiveRoomUvAndPvStream = tableEnv.toRetractStream(factLiveRoomPvAndUvTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomUvAndPv>() {
            @Override
            public FactLiveRoomUvAndPv map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomPvAndUv(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomUvAndPv>() {
            @Override
            public boolean filter(FactLiveRoomUvAndPv value) throws Exception {
                return value != null;
            }
        });

        DataStream<FactLiveRoomUvByShare> factLiveRoomUvByShareStream = tableEnv.toRetractStream(factLiveRoomUvByShareTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomUvByShare>() {
            @Override
            public FactLiveRoomUvByShare map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomUvByShare(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomUvByShare>() {
            @Override
            public boolean filter(FactLiveRoomUvByShare value) throws Exception {
                return value != null;
            }
        });

        DataStream<FactLiveRoomReceivedCoupon> factLiveRoomReceivedCouponStream = tableEnv.toRetractStream(factLiveRoomReceivedCouponTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomReceivedCoupon>() {
            @Override
            public FactLiveRoomReceivedCoupon map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomReceivedCoupon(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomReceivedCoupon>() {
            @Override
            public boolean filter(FactLiveRoomReceivedCoupon value) throws Exception {
                return value != null;
            }
        });

        DataStream<FactLiveRoomOrder> factLiveRoomOrderStream = tableEnv.toRetractStream(factLiveRoomOrderTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomOrder>() {
            @Override
            public FactLiveRoomOrder map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomOrder(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomOrder>() {
            @Override
            public boolean filter(FactLiveRoomOrder value) throws Exception {
                return value != null;
            }
        });

        DataStream<FactLiveRoomNewNumber> factLiveRoomNewNumberStream = tableEnv.toRetractStream(factLiveRoomNewNumberTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomNewNumber>() {
            @Override
            public FactLiveRoomNewNumber map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomNewNumber(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomNewNumber>() {
            @Override
            public boolean filter(FactLiveRoomNewNumber value) throws Exception {
                return value != null;
            }
        });

        DataStream<FactLiveRoomProductOrder> factLiveRoomProductOrderStream = tableEnv.toRetractStream(factLiveRoomProductOrderTable, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactLiveRoomProductOrder>() {
            @Override
            public FactLiveRoomProductOrder map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactLiveRoomProductOrder(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactLiveRoomProductOrder>() {
            @Override
            public boolean filter(FactLiveRoomProductOrder value) throws Exception {
                return value != null;
            }
        });


        //推送流
        DataStream<LiveRoomNotice> liveRoomNoticeLog = odsLogStream.map(new MapFunction<OdsLogStream, LiveRoomNotice>() {
            @Override
            public LiveRoomNotice map(OdsLogStream value) throws Exception {
                LiveRoomNotice liveRoomNotice = new LiveRoomNotice();

                liveRoomNotice.setShopId(value.getShopId());
                liveRoomNotice.setLiveRoomId(value.getLiveRoomId());
                liveRoomNotice.setMemberId(value.getUserId());
                liveRoomNotice.setMemberPhone(value.getUserPhone());
                String openId = value.getOpenId() != null && value.getOpenId().length() >= 4 ? value.getOpenId().substring(value.getOpenId().length() - 4) : null;
                liveRoomNotice.setMemberName(value.getUserId() != 0 ? value.getUserName() + " " + value.getUserPhone() : "用户 " + openId);
                liveRoomNotice.setPushTime(Timestamp.from(Instant.now()));
                switch (value.getEventName()) {
                    case "EnterLiveRoom":
                        liveRoomNotice.setNoticeType(0);
                        break;
                    case "ViewGoodsByActivity":
                        if(value.getPromotionType() == null){
                            return null;
                        }
                        if (value.getPromotionType() == 4 && value.getEndTime() == null) {
                            liveRoomNotice.setNoticeType(3);
                            liveRoomNotice.setLiveRoomId(value.getPromotionId());
                            liveRoomNotice.setProductId(value.getGoodsId());
                            liveRoomNotice.setProductName(value.getGoodsName());
                        }
                        break;
                    case "ViewCouponByLiveRoom":
                        liveRoomNotice.setNoticeType(5);
                        if (value.getPromotionType() == 1) {
                            liveRoomNotice.setCouponId(value.getPromotionId());
                        }
                        liveRoomNotice.setCouponName(value.getCouponName());
                        break;
                    case "ReceiveCouponByLiveRoom":
                        liveRoomNotice.setNoticeType(6);
                        if (value.getPromotionType() == 1) {
                            liveRoomNotice.setCouponId(value.getPromotionId());
                        }
                        liveRoomNotice.setCouponName(value.getCouponName());
                        break;
                    case "StartLiveRoom":
                        liveRoomNotice.setNoticeType(7);
                        break;
                    case "EndLiveRoom":
                        liveRoomNotice.setNoticeType(8);
                        break;
                    default:
                        break;
                }
                return liveRoomNotice;
            }
        }).filter(new FilterFunction<LiveRoomNotice>() {
            @Override
            public boolean filter(LiveRoomNotice value) throws Exception {
                return value != null && value.getNoticeType() != null;
            }
        });

        //推送点击优惠劵
        DataStream<LiveRoomUserReceiveCoupon> liveRoomUserReceiveCouponDataStream = odsLogStream.map(new MapFunction<OdsLogStream, LiveRoomUserReceiveCoupon>() {
            @Override
            public LiveRoomUserReceiveCoupon map(OdsLogStream value) throws Exception {
                if ("ReceiveCouponByLiveRoom".equals(value.getEventName())) {
                    LiveRoomUserReceiveCoupon liveRoomUserReceiveCoupon = new LiveRoomUserReceiveCoupon();

                    liveRoomUserReceiveCoupon.setShopId(value.getShopId());
                    liveRoomUserReceiveCoupon.setLiveRoomId(value.getLiveRoomId());
                    liveRoomUserReceiveCoupon.setMemberId(value.getUserId());
                    liveRoomUserReceiveCoupon.setMemberPhone(value.getUserPhone());
                    liveRoomUserReceiveCoupon.setMemberName(value.getUserName());
                    liveRoomUserReceiveCoupon.setProductType(value.getProductType());
                    liveRoomUserReceiveCoupon.setCouponName(value.getCouponName());
                    if (value.getPromotionType() == 1) {
                        liveRoomUserReceiveCoupon.setCouponId(value.getPromotionId());
                    }
                    return liveRoomUserReceiveCoupon;
                }
                return null;
            }
        }).filter(new FilterFunction<LiveRoomUserReceiveCoupon>() {
            @Override
            public boolean filter(LiveRoomUserReceiveCoupon value) throws Exception {
                return value != null;
            }
        });


        DataStream<LiveRoomNotice> liveRoomNoticeProductOrder = odsLiveRoomOrderItemDataStream.map(new MapFunction<OdsLiveRoomOrderItem, LiveRoomNotice>() {
            @Override
            public LiveRoomNotice map(OdsLiveRoomOrderItem value) throws Exception {
                if (value.getStatus() == 1 && (value.getProductType() == 1 || value.getProductType() == 2 || value.getProductType() == 3)) {
                    LiveRoomNotice liveRoomNotice = new LiveRoomNotice();

                    liveRoomNotice.setNoticeType(4);
                    liveRoomNotice.setShopId(value.getShopId());
                    liveRoomNotice.setLiveRoomId(value.getLiveRoomId());
                    liveRoomNotice.setMemberId(value.getMemberId());
                    liveRoomNotice.setMemberName(value.getMemberName());
                    liveRoomNotice.setMemberPhone(value.getMemberPhone());
                    liveRoomNotice.setProductId(value.getProductId());
                    liveRoomNotice.setProductName(value.getProductName());
                    liveRoomNotice.setProductCount(value.getQuantity());
                    liveRoomNotice.setPushTime(Timestamp.from(Instant.now()));
                    return liveRoomNotice;
                }
                return null;
            }
        }).filter(new FilterFunction<LiveRoomNotice>() {
            @Override
            public boolean filter(LiveRoomNotice value) throws Exception {
                return value != null;
            }
        });

        DataStream<LiveRoomNotice> liveRoomNoticeStreamUv = factLiveRoomUvAndPvStream.map(new MapFunction<FactLiveRoomUvAndPv, LiveRoomNotice>() {
            @Override
            public LiveRoomNotice map(FactLiveRoomUvAndPv value) throws Exception {
                long index = numberCount(value.getUv()) == 1 ? 1 : numberCount(value.getUv()) - 1;
                if (value.getUv() % Math.pow(10, index) == 0) {
                    LiveRoomNotice liveRoomNotice = new LiveRoomNotice();
                    liveRoomNotice.setNoticeType(1);
                    liveRoomNotice.setShopId(value.getShopId());
                    liveRoomNotice.setLiveRoomId(value.getLiveRoomId());
                    liveRoomNotice.setUv(value.getUv());
                    liveRoomNotice.setPushTime(Timestamp.from(Instant.now()));
                    return liveRoomNotice;
                }
                return null;
            }
        }).filter(new FilterFunction<LiveRoomNotice>() {
            @Override
            public boolean filter(LiveRoomNotice value) throws Exception {
                return value != null;
            }
        });

        DataStream<LiveRoomNotice> liveRoomNoticeOrder = factLiveRoomOrderStream
                .keyBy("shopId", "liveRoomId")
                .map(new LiveRoomNoticeFunction())
                .uid("live-room-notify-map-op")
                .name("live-room-notify-map")
                .filter(new FilterFunction<LiveRoomNotice>() {
                    @Override
                    public boolean filter(LiveRoomNotice value) throws Exception {
                        return value != null;
                    }
                }).uid("live-room-notify-filter-op")
                .name("live-room-notify-filter");

        //动态推送消息写入kafka
        toKafkaSink(liveRoomNoticeStreamUv, "live_room_notice");
        toKafkaSink(liveRoomNoticeOrder, "live_room_notice");
        toKafkaSink(liveRoomNoticeLog, "live_room_notice");
        toKafkaSink(liveRoomNoticeProductOrder, "live_room_notice");
        toKafkaSink(liveRoomUserReceiveCouponDataStream, "wx_live_room_user_receive_coupon_topic");

        //计算指标写入kafka
        toKafkaSink(factLiveRoomUvAndPvStream, "fact_live_room_uv_pv");
        toKafkaSink(factLiveRoomUvByShareStream, "fact_live_room_share");
        toKafkaSink(factLiveRoomOrderStream, "fact_live_room_order");
        toKafkaSink(factLiveRoomReceivedCouponStream, "fact_live_room_received_coupon");
        toKafkaSink(factLiveRoomNewNumberStream, "fact_live_room_new_member");
        toKafkaSink(factLiveRoomProductOrderStream, "fact_live_room_product_order");

        //写入mysql
        toJdbcUpsertSink(factLiveRoomUvAndPvStream, "fact_live_room", FactLiveRoomUvAndPv.class);
        toJdbcUpsertSink(factLiveRoomUvByShareStream, "fact_live_room", FactLiveRoomUvByShare.class);
        toJdbcUpsertSink(factLiveRoomOrderStream, "fact_live_room", FactLiveRoomOrder.class);
        toJdbcUpsertSink(factLiveRoomReceivedCouponStream, "fact_live_room", FactLiveRoomReceivedCoupon.class);
        toJdbcUpsertSink(factLiveRoomNewNumberStream, "fact_live_room", FactLiveRoomNewNumber.class);
        toJdbcUpsertSink(factLiveRoomProductOrderStream, "fact_live_room", FactLiveRoomProductOrder.class);
    }

    /**
     * pv、uv
     *
     * @param row
     * @return
     */
    private static FactLiveRoomUvAndPv convertToFactLiveRoomPvAndUv(Row row) {
        FactLiveRoomUvAndPv factLiveRoomUvAndPv = new FactLiveRoomUvAndPv();

        factLiveRoomUvAndPv.setShopId((Integer) row.getField(0));
        factLiveRoomUvAndPv.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomUvAndPv.setPv((Long) row.getField(2));
        factLiveRoomUvAndPv.setUv((Long) row.getField(3));

        return factLiveRoomUvAndPv;
    }

    /**
     * uv_share
     *
     * @param row
     * @return
     */
    private static FactLiveRoomUvByShare convertToFactLiveRoomUvByShare(Row row) {
        FactLiveRoomUvByShare factLiveRoomUvByShare = new FactLiveRoomUvByShare();

        factLiveRoomUvByShare.setShopId((Integer) row.getField(0));
        factLiveRoomUvByShare.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomUvByShare.setUvByShare((Long) row.getField(2));

        return factLiveRoomUvByShare;
    }

    /**
     * received_count
     *
     * @param row
     * @return
     */
    private static FactLiveRoomReceivedCoupon convertToFactLiveRoomReceivedCoupon(Row row) {
        FactLiveRoomReceivedCoupon factLiveRoomReceivedCoupon = new FactLiveRoomReceivedCoupon();

        factLiveRoomReceivedCoupon.setShopId((Integer) row.getField(0));
        factLiveRoomReceivedCoupon.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomReceivedCoupon.setRevivedCouponCount((Long) row.getField(2));

        return factLiveRoomReceivedCoupon;
    }

    /**
     * order
     *
     * @param row
     * @return
     */
    private static FactLiveRoomOrder convertToFactLiveRoomOrder(Row row) {
        FactLiveRoomOrder factLiveRoomOrder = new FactLiveRoomOrder();

        factLiveRoomOrder.setShopId((Integer) row.getField(0));
        factLiveRoomOrder.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomOrder.setOrderCount((Long) row.getField(2));
        factLiveRoomOrder.setPayNumber((Long) row.getField(3));
        factLiveRoomOrder.setOrderAmount((BigDecimal) row.getField(4));

        return factLiveRoomOrder;
    }

    /**
     * product_order
     *
     * @param row
     * @return
     */
    private static FactLiveRoomProductOrder convertToFactLiveRoomProductOrder(Row row) {
        FactLiveRoomProductOrder factLiveRoomProductOrder = new FactLiveRoomProductOrder();

        factLiveRoomProductOrder.setShopId((Integer) row.getField(0));
        factLiveRoomProductOrder.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomProductOrder.setProductAmount((BigDecimal) row.getField(2));
        factLiveRoomProductOrder.setProductCount((Integer) row.getField(3));
        return factLiveRoomProductOrder;
    }

    /**
     * new number
     *
     * @param row
     * @return
     */
    private static FactLiveRoomNewNumber convertToFactLiveRoomNewNumber(Row row) {
        FactLiveRoomNewNumber factLiveRoomNewNumber = new FactLiveRoomNewNumber();

        factLiveRoomNewNumber.setShopId((Integer) row.getField(0));
        factLiveRoomNewNumber.setLiveRoomId((Integer) row.getField(1));
        factLiveRoomNewNumber.setNewNumber((Long) row.getField(2));

        return factLiveRoomNewNumber;
    }


    /**
     * 判断一个整数是几位数
     * 看可以被10整除几次
     *
     * @param value
     * @return
     */
    private static Integer numberCount(Long value) {
        int count = 0; //计数
        while (value >= 1) {
            value /= 10;
            count++;
        }
        return count;
    }

    /**
     * 初始化推送阶数 最大值已到 9*(10^12)=千亿
     * 100、200、300、1000、2000 ....
     *
     * @return
     */
    private static List<BigDecimal> init() {
        List<BigDecimal> list = new ArrayList<>();
        Integer[] baseNumber = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] indexNumber = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        for (Integer integer : indexNumber) {
            double pow = Math.pow(10, integer);
            for (Integer integer1 : baseNumber) {
                double v = pow * integer1;
                list.add(BigDecimal.valueOf(v));
            }
        }
        return list;
    }
}
