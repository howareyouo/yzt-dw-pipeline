package cn.yizhi.yzt.pipeline.jobs.batchJob;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.shop.FactShop;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.ShopMember;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author hucheng
 * 计算店铺累加数据
 * @date 2020/11/10 下午2:29
 */
public class FactShopBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> odsEmallOrderRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsEmallOrder.class,
                "ods_emall_order",
                null)
                .name("ds-ods_emall_order");


        DataSet<Row> odsShopMemberRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ShopMember.class,
                "ods_shop_member",
                null)
                .name("ds-ods_shop_member");


        OutputFormat<FactShop> outputFormat =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactShop.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_SHOP)
                        .finish();

        DataSet<OdsEmallOrder> odsEmallOrderDs = odsEmallOrderRow
                .map(new MapFunction<Row, OdsEmallOrder>() {
                    @Override
                    public OdsEmallOrder map(Row value) throws Exception {
                        return PojoTypes.of(OdsEmallOrder.class).fromRow(value);
                    }
                })
                .flatMap(new FlatMapFunction<OdsEmallOrder, OdsEmallOrder>() {
                    @Override
                    public void flatMap(OdsEmallOrder value, Collector<OdsEmallOrder> out) throws Exception {
                        if ("wxweb".equals(value.getSource())) {
                            value.setSource("web");
                        }
                        out.collect(value);

                        //全渠道
                        value.setSource("all");
                        out.collect(value);
                    }
                });

        //累计退款
        DataSet<FactShopAccumulationRefund> refundReduceDs = odsEmallOrderDs
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getStatus() == 9;
                    }
                })
                .flatMap(new FlatMapFunction<OdsEmallOrder, FactShopAccumulationRefund>() {
                    @Override
                    public void flatMap(OdsEmallOrder value, Collector<FactShopAccumulationRefund> out) throws Exception {
                        FactShopAccumulationRefund factShopAccumulationRefund = new FactShopAccumulationRefund();
                        factShopAccumulationRefund.shopId = value.getShopId();
                        factShopAccumulationRefund.channel = value.getSource();
                        factShopAccumulationRefund.refundAmount = value.getActualAmount();
                        factShopAccumulationRefund.refundCount = 1;

                        out.collect(factShopAccumulationRefund);
                    }
                })
                .groupBy(new KeySelector<FactShopAccumulationRefund, Tuple2<Integer, String>>() {

                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationRefund value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .reduce(new RefundReduceFunction());


        DataSet<OdsEmallOrder> orderFilter =
                odsEmallOrderDs
                        .filter(new FilterFunction<OdsEmallOrder>() {
                            @Override
                            public boolean filter(OdsEmallOrder value) throws Exception {
                                return value.getTransactionNo() != null && !value.getTransactionNo().equals("");
                            }
                        });

        //累计订单额
        DataSet<FactShopAccumulationOrder> orderReduceDs =
                orderFilter
                        .flatMap(new FlatMapFunction<OdsEmallOrder, FactShopAccumulationOrder>() {
                            @Override
                            public void flatMap(OdsEmallOrder value, Collector<FactShopAccumulationOrder> out) throws Exception {
                                FactShopAccumulationOrder factShopAccumulationOrder = new FactShopAccumulationOrder();
                                factShopAccumulationOrder.shopId = value.getShopId();
                                factShopAccumulationOrder.channel = value.getSource();
                                factShopAccumulationOrder.orderAmount = value.getActualAmount();
                                factShopAccumulationOrder.orderCount = 1;

                                out.collect(factShopAccumulationOrder);
                            }
                        })
                        .groupBy(new KeySelector<FactShopAccumulationOrder, Tuple2<Integer, String>>() {
                            @Override
                            public Tuple2<Integer, String> getKey(FactShopAccumulationOrder value) throws Exception {
                                return new Tuple2<>(value.shopId, value.channel);
                            }
                        })
                        .reduce(new OrderReduceFunction());

        DataSet<ShopMember> shopMemberFlatMapDs =
                odsShopMemberRow
                        .map(new MapFunction<Row, ShopMember>() {
                            @Override
                            public ShopMember map(Row value) throws Exception {
                                ShopMember shopMember = PojoTypes.of(ShopMember.class).fromRow(value);
                                if (!"wxapp".equals(shopMember.getSource())) {
                                    shopMember.setSource("web");
                                }
                                return shopMember;
                            }
                        })
                        .filter(new FilterFunction<ShopMember>() {
                            @Override
                            public boolean filter(ShopMember value) throws Exception {
                                return value.getDisabled() == 0;
                            }
                        })
                        .flatMap(new FlatMapFunction<ShopMember, ShopMember>() {
                            @Override
                            public void flatMap(ShopMember value, Collector<ShopMember> out) throws Exception {
                                out.collect(value);

                                //全渠道
                                value.setSource("all");
                                out.collect(value);
                            }
                        });

        //累计订单人数需要和shop_member表先关联，并指定disable == 0
        DataSet<FactShopAccumulationOrderUser> orderUserReduceChannelDs = shopMemberFlatMapDs
                .join(orderFilter)
                .where(new KeySelector<ShopMember, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(ShopMember value) throws Exception {
                        return new Tuple1<>(value.getId());
                    }
                })
                .equalTo(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getMemberId());
                    }
                })
                .with(new JoinFunction<ShopMember, OdsEmallOrder, ShopMember>() {
                    @Override
                    public ShopMember join(ShopMember first, OdsEmallOrder second) throws Exception {
                        if (first.getId().equals(second.getMemberId())) {
                            return first;
                        }
                        return null;
                    }
                })
                .filter(new FilterFunction<ShopMember>() {
                    @Override
                    public boolean filter(ShopMember value) throws Exception {
                        return value != null;
                    }
                }).distinct(new KeySelector<ShopMember, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> getKey(ShopMember value) throws Exception {
                        return new Tuple2<>(value.getSource(), value.getMemberId());
                    }
                })
                .flatMap(new FlatMapFunction<ShopMember, FactShopAccumulationOrderUser>() {
                    @Override
                    public void flatMap(ShopMember value, Collector<FactShopAccumulationOrderUser> out) throws Exception {
                        FactShopAccumulationOrderUser factShopAccumulationOrderUser = new FactShopAccumulationOrderUser();
                        factShopAccumulationOrderUser.shopId = value.getShopId();
                        factShopAccumulationOrderUser.channel = value.getSource();
                        factShopAccumulationOrderUser.orderUserCount = 1;

                        out.collect(factShopAccumulationOrderUser);
                    }
                })
                .groupBy(new KeySelector<FactShopAccumulationOrderUser, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationOrderUser value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                }).reduce(new OrderUserReduceFunction());


        //累计客户数
        DataSet<FactShopAccumulationMember> memberReduceDs = shopMemberFlatMapDs
                .flatMap(new FlatMapFunction<ShopMember, FactShopAccumulationMember>() {
                    @Override
                    public void flatMap(ShopMember value, Collector<FactShopAccumulationMember> out) throws Exception {
                        FactShopAccumulationMember factShopAccumulationMember = new FactShopAccumulationMember();
                        factShopAccumulationMember.shopId = value.getShopId();
                        factShopAccumulationMember.channel = value.getSource();
                        factShopAccumulationMember.memberCount = 1;

                        out.collect(factShopAccumulationMember);
                    }
                })
                .groupBy(new KeySelector<FactShopAccumulationMember, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationMember value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .reduce(new memberReduceFunction());


        //连接计算结果
        DataSet<FactShop> orderFactShopChannelDs = refundReduceDs
                .rightOuterJoin(orderReduceDs)
                .where(new KeySelector<FactShopAccumulationRefund, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationRefund value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .equalTo(new KeySelector<FactShopAccumulationOrder, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationOrder value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .with(new JoinFunction<FactShopAccumulationRefund, FactShopAccumulationOrder, FactShop>() {
                    @Override
                    public FactShop join(FactShopAccumulationRefund first, FactShopAccumulationOrder second) throws Exception {
                        FactShop factShop = new FactShop();
                        factShop.setShopId(second.shopId);
                        factShop.setChannel(second.channel);
                        factShop.setOrderAmount(second.orderAmount);
                        factShop.setOrderCount(second.orderCount);
                        if (first != null) {
                            factShop.setRefundAmount(first.refundAmount);
                            factShop.setRefundCount(first.refundCount);
                        } else {
                            factShop.setRefundCount(0);
                            factShop.setRefundAmount(BigDecimal.ZERO);
                        }
                        return factShop;
                    }
                })
                .join(orderUserReduceChannelDs)
                .where(new KeySelector<FactShop, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShop value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getChannel());
                    }
                })
                .equalTo(new KeySelector<FactShopAccumulationOrderUser, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationOrderUser value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .with(new JoinFunction<FactShop, FactShopAccumulationOrderUser, FactShop>() {
                    @Override
                    public FactShop join(FactShop first, FactShopAccumulationOrderUser second) throws Exception {
                        first.setOrderUserCount(second.orderUserCount);
                        return first;
                    }
                });

        memberReduceDs
                .leftOuterJoin(orderFactShopChannelDs)
                .where(new KeySelector<FactShopAccumulationMember, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShopAccumulationMember value) throws Exception {
                        return new Tuple2<>(value.shopId, value.channel);
                    }
                })
                .equalTo(new KeySelector<FactShop, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(FactShop value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getChannel());
                    }
                })
                .with(new JoinFunction<FactShopAccumulationMember, FactShop, FactShop>() {
                    @Override
                    public FactShop join(FactShopAccumulationMember first, FactShop second) throws Exception {
                        FactShop factShop = new FactShop();
                        factShop.setShopId(first.shopId);
                        factShop.setChannel(first.channel);
                        factShop.setMemberCount(first.memberCount);
                        factShop.setRowTime(LocalDate.now().toString());
                        if (second != null) {
                            factShop.setOrderUserCount(second.getOrderUserCount());
                            factShop.setOrderCount(second.getOrderCount());
                            factShop.setOrderAmount(second.getOrderAmount() != null ? second.getOrderAmount() : BigDecimal.ZERO);
                            factShop.setRefundCount(second.getRefundCount());
                            factShop.setRefundAmount(second.getRefundAmount() != null ? second.getRefundAmount() : BigDecimal.ZERO);
                            factShop.setNotOrderUserCount(first.memberCount - second.getOrderUserCount());
                        } else {
                            factShop.setRefundAmount(BigDecimal.ZERO);
                            factShop.setOrderAmount(BigDecimal.ZERO);
                            factShop.setNotOrderUserCount(first.memberCount);
                        }
                        return factShop;
                    }
                })
                .output(outputFormat);
    }

    /**
     * 店铺累加退款
     */
    public static class FactShopAccumulationRefund {
        public int shopId;
        public String channel;

        /**
         * 累计退款单数
         */
        public int refundCount = 0;

        /**
         * 累计退款金额
         */
        public BigDecimal refundAmount = BigDecimal.ZERO;

    }


    /**
     * 店铺累计订单
     */
    public static class FactShopAccumulationOrder {
        public int shopId;

        public String channel;

        /**
         * 累计付款单数
         */
        public int orderCount = 0;

        /**
         * 累计付款金额
         */
        public BigDecimal orderAmount = BigDecimal.ZERO;

    }


    /**
     * 店铺累计订单人数
     */
    public static class FactShopAccumulationOrderUser {
        public int shopId;

        public String channel;

        /**
         * 累计付款人数
         */
        public int orderUserCount = 0;

    }


    /**
     * 店铺累计客户数
     */
    public static class FactShopAccumulationMember {
        public int shopId;

        public String channel;

        /**
         * 累计客户数
         */
        public int memberCount = 0;

    }

    public static class RefundReduceFunction implements ReduceFunction<FactShopAccumulationRefund> {

        @Override
        public FactShopAccumulationRefund reduce(FactShopAccumulationRefund value1, FactShopAccumulationRefund value2) throws Exception {
            FactShopAccumulationRefund fr = new FactShopAccumulationRefund();
            fr.shopId = value1.shopId;
            fr.channel = value2.channel;
            fr.refundAmount = value1.refundAmount.add(value2.refundAmount);
            fr.refundCount = value1.refundCount + value2.refundCount;
            return fr;
        }
    }


    public static class OrderReduceFunction implements ReduceFunction<FactShopAccumulationOrder> {

        @Override
        public FactShopAccumulationOrder reduce(FactShopAccumulationOrder value1, FactShopAccumulationOrder value2) throws Exception {
            FactShopAccumulationOrder fo = new FactShopAccumulationOrder();
            fo.shopId = value1.shopId;
            fo.channel = value2.channel;
            fo.orderAmount = value1.orderAmount.add(value2.orderAmount);
            fo.orderCount = value1.orderCount + value2.orderCount;
            return fo;
        }
    }

    public static class OrderUserReduceFunction implements ReduceFunction<FactShopAccumulationOrderUser> {

        @Override
        public FactShopAccumulationOrderUser reduce(FactShopAccumulationOrderUser value1, FactShopAccumulationOrderUser value2) throws Exception {
            FactShopAccumulationOrderUser fo = new FactShopAccumulationOrderUser();
            fo.shopId = value1.shopId;
            fo.channel = value2.channel;
            fo.orderUserCount = value1.orderUserCount + value2.orderUserCount;
            return fo;
        }
    }


    public static class memberReduceFunction implements ReduceFunction<FactShopAccumulationMember> {

        @Override
        public FactShopAccumulationMember reduce(FactShopAccumulationMember value1, FactShopAccumulationMember value2) throws Exception {
            FactShopAccumulationMember fm = new FactShopAccumulationMember();
            fm.shopId = value1.shopId;
            fm.channel = value2.channel;
            fm.memberCount = value1.memberCount + value2.memberCount;
            return fm;
        }
    }
}
