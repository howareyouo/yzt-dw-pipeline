package cn.yizhi.yzt.pipeline.jobs.batchJob;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberFirstOrder;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionAll;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
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
 * @Author: HuCheng
 * @Date: 2020/11/23 16:05
 */
public class FlashSaleBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> odsEmallOrderRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsEmallOrder.class,
                "ods_emall_order",
                null)
                .name("ds-ods_emall_order");

        DataSet<Row> odsOrderItemRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsOrderItem.class,
                "ods_order_item",
                null)
                .name("ds-ods_order_item");


        DataSet<Row> factMemberFirstOrder = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactMemberFirstOrder.class,
                "fact_member_first_order",
                null)
                .name("ds-fact_member_first_order");


        DataSet<OdsEmallOrder> odsEmallOrderFilterDs = odsEmallOrderRow
                .map(new MapFunction<Row, OdsEmallOrder>() {
                    @Override
                    public OdsEmallOrder map(Row value) throws Exception {
                        return PojoTypes.of(OdsEmallOrder.class).fromRow(value);
                    }
                })
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getOrderType() == 3;
                    }
                });

        DataSet<OdsOrderItem> odsOrderItemMapDs = odsOrderItemRow
                .map(new MapFunction<Row, OdsOrderItem>() {
                    @Override
                    public OdsOrderItem map(Row value) throws Exception {
                        return PojoTypes.of(OdsOrderItem.class).fromRow(value);
                    }
                });

        DataSet<FactMemberFirstOrder> factMemberFirstOrderMapDs = factMemberFirstOrder
                .map(new MapFunction<Row, FactMemberFirstOrder>() {
                    @Override
                    public FactMemberFirstOrder map(Row value) throws Exception {
                        return PojoTypes.of(FactMemberFirstOrder.class).fromRow(value);
                    }
                })
                .filter(new FilterFunction<FactMemberFirstOrder>() {
                    @Override
                    public boolean filter(FactMemberFirstOrder value) throws Exception {
                        return value.getPromotionType() == 3;
                    }
                })
                .distinct(new KeySelector<FactMemberFirstOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactMemberFirstOrder value) throws Exception {
                        return new Tuple1<>(value.getMemberId());
                    }
                });


        OutputFormat<FactPromotionAll> outputFormat =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactPromotionAll.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_PROMOTION_ALL)
                        .finish();


        DataSet<OrderItemJoin> odsOrderItemDs = odsEmallOrderFilterDs
                .join(odsOrderItemMapDs)
                .where(new KeySelector<OdsEmallOrder, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getId());
                    }
                })
                .equalTo(new KeySelector<OdsOrderItem, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsOrderItem value) throws Exception {
                        return new Tuple1<>(value.getOrderId());
                    }
                })
                .with(new JoinFunction<OdsEmallOrder, OdsOrderItem, OrderItemJoin>() {
                    @Override
                    public OrderItemJoin join(OdsEmallOrder first, OdsOrderItem second) throws Exception {
                        OrderItemJoin orderItemJoin = new OrderItemJoin();
                        orderItemJoin.odsEmallOrder = first;
                        orderItemJoin.odsOrderItem = second;
                        return orderItemJoin;
                    }
                });

        //累计付款和累计订单金额
        DataSet<FactPromotionOrder> factPromotionOrderReduceDs = odsOrderItemDs
                .filter(new FilterFunction<OrderItemJoin>() {
                    @Override
                    public boolean filter(OrderItemJoin value) throws Exception {
                        return value.odsEmallOrder.getTransactionNo() != null && !"".equals(value.odsEmallOrder.getTransactionNo());
                    }
                })
                .flatMap(new FlatMapFunction<OrderItemJoin, FactPromotionOrder>() {
                    @Override
                    public void flatMap(OrderItemJoin value, Collector<FactPromotionOrder> out) throws Exception {
                        FactPromotionOrder factPromotionOrder = new FactPromotionOrder();
                        factPromotionOrder.shopId = value.odsEmallOrder.getShopId();
                        factPromotionOrder.promotionType = 3;
                        factPromotionOrder.orderCount = 1;
                        factPromotionOrder.orderAmount = value.odsEmallOrder.getActualAmount();
                        factPromotionOrder.discountAmount = value.odsOrderItem.getDiscountAmount();

                        out.collect(factPromotionOrder);
                    }
                })
                .groupBy(new KeySelector<FactPromotionOrder, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionOrder value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .reduce(new OrderFunction());

        //累计退款和累计退款金额
        DataSet<FactPromotionRefund> factPromotionRefundReduceDs = odsOrderItemDs
                .filter(new FilterFunction<OrderItemJoin>() {
                    @Override
                    public boolean filter(OrderItemJoin value) throws Exception {
                        return value.odsEmallOrder.getStatus() == 9;
                    }
                })
                .flatMap(new FlatMapFunction<OrderItemJoin, FactPromotionRefund>() {
                    @Override
                    public void flatMap(OrderItemJoin value, Collector<FactPromotionRefund> out) throws Exception {
                        FactPromotionRefund factPromotionRefund = new FactPromotionRefund();
                        factPromotionRefund.shopId = value.odsEmallOrder.getShopId();
                        factPromotionRefund.promotionType = 3;
                        factPromotionRefund.refundCount = 1;
                        factPromotionRefund.refundAmount = value.odsEmallOrder.getActualAmount();

                        out.collect(factPromotionRefund);
                    }
                })
                .groupBy(new KeySelector<FactPromotionRefund, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionRefund value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .reduce(new RefundFunction());

        //新客
        DataSet<FactPromotionNewOrderMember> factPromotionNewOrderMemberReduceDs = factMemberFirstOrderMapDs
                .filter(new FilterFunction<FactMemberFirstOrder>() {
                    @Override
                    public boolean filter(FactMemberFirstOrder value) throws Exception {
                        return value.getPromotionType() != 0;
                    }
                })
                .flatMap(new FlatMapFunction<FactMemberFirstOrder, FactPromotionNewOrderMember>() {
                    @Override
                    public void flatMap(FactMemberFirstOrder value, Collector<FactPromotionNewOrderMember> out) throws Exception {
                        FactPromotionNewOrderMember factPromotionNewOrderMember = new FactPromotionNewOrderMember();
                        factPromotionNewOrderMember.shopId = value.getShopId();
                        factPromotionNewOrderMember.promotionType = value.getPromotionType();
                        factPromotionNewOrderMember.newOrderUserCount = 1;

                        out.collect(factPromotionNewOrderMember);
                    }
                })
                .groupBy(new KeySelector<FactPromotionNewOrderMember, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionNewOrderMember value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .reduce(new NewOrderUserFunction());


        //连接最后的结果
        factPromotionOrderReduceDs
                .leftOuterJoin(factPromotionRefundReduceDs)
                .where(new KeySelector<FactPromotionOrder, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionOrder value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .equalTo(new KeySelector<FactPromotionRefund, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionRefund value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .with(new JoinFunction<FactPromotionOrder, FactPromotionRefund, FactPromotionAll>() {
                    @Override
                    public FactPromotionAll join(FactPromotionOrder first, FactPromotionRefund second) throws Exception {
                        FactPromotionAll factPromotionAll = new FactPromotionAll();
                        factPromotionAll.setRowDate(LocalDate.now().toString());
                        factPromotionAll.setShopId(first.shopId);
                        factPromotionAll.setPromotionType(first.promotionType);
                        factPromotionAll.setOrderCount(first.orderCount);
                        factPromotionAll.setOrderAmount(first.orderAmount);
                        factPromotionAll.setDiscountAmount(first.discountAmount);

                        if (second != null) {
                            factPromotionAll.setRefundCount(second.refundCount);
                            factPromotionAll.setRefundAmount(second.refundAmount);
                        } else {
                            factPromotionAll.setRefundCount(0);
                            factPromotionAll.setRefundAmount(BigDecimal.ZERO);
                        }
                        return factPromotionAll;
                    }
                })
                .leftOuterJoin(factPromotionNewOrderMemberReduceDs)
                .where(new KeySelector<FactPromotionAll, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionAll value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getPromotionType());
                    }
                })
                .equalTo(new KeySelector<FactPromotionNewOrderMember, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPromotionNewOrderMember value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionType);
                    }
                })
                .with(new JoinFunction<FactPromotionAll, FactPromotionNewOrderMember, FactPromotionAll>() {
                    @Override
                    public FactPromotionAll join(FactPromotionAll first, FactPromotionNewOrderMember second) throws Exception {
                        if (second != null) {
                            first.setNewOrderMember(second.newOrderUserCount);
                        } else {
                            first.setNewOrderMember(0);
                        }

                        return first;
                    }
                })
                .output(outputFormat);
    }

    private static class OrderItemJoin {
        private OdsEmallOrder odsEmallOrder;
        private OdsOrderItem odsOrderItem;
    }

    /**
     * 累积付款人数
     */
    public static class FactPromotionOrder {

        public int shopId;

        /**
         * 活动类型
         */
        public int promotionType;

        /**
         * 累计付款人数
         */
        public int orderCount = 0;

        /**
         * 累计付款金额
         */
        public BigDecimal orderAmount = BigDecimal.ZERO;

        /**
         * 累计优惠金额
         */
        public BigDecimal discountAmount = BigDecimal.ZERO;

    }


    /**
     * 累积付款人数
     */
    public static class FactPromotionRefund {

        public int shopId;

        /**
         * 活动类型
         */
        public int promotionType;

        /**
         * 累计退款人数
         */
        public int refundCount = 0;

        /**
         * 累计退款金额
         */
        public BigDecimal refundAmount = BigDecimal.ZERO;

    }


    /**
     * 累积新客
     */
    public static class FactPromotionNewOrderMember {

        public int shopId;

        /**
         * 活动类型
         */
        public int promotionType;

        /**
         * 累计拓新客数
         */
        public int newOrderUserCount = 0;
    }


    public static class OrderFunction implements ReduceFunction<FactPromotionOrder> {

        @Override
        public FactPromotionOrder reduce(FactPromotionOrder value1, FactPromotionOrder value2) throws Exception {
            FactPromotionOrder factPromotionOrder = new FactPromotionOrder();
            factPromotionOrder.shopId = value1.shopId;
            factPromotionOrder.promotionType = value1.promotionType;
            factPromotionOrder.orderCount = value1.orderCount + value2.orderCount;
            factPromotionOrder.orderAmount = value1.orderAmount.add(value2.orderAmount);
            factPromotionOrder.discountAmount = value1.discountAmount.add(value2.discountAmount);

            return factPromotionOrder;
        }
    }


    public static class RefundFunction implements ReduceFunction<FactPromotionRefund> {

        @Override
        public FactPromotionRefund reduce(FactPromotionRefund value1, FactPromotionRefund value2) throws Exception {
            FactPromotionRefund factPromotionRefund = new FactPromotionRefund();
            factPromotionRefund.shopId = value1.shopId;
            factPromotionRefund.promotionType = value1.promotionType;
            factPromotionRefund.refundCount = value1.refundCount + value2.refundCount;
            factPromotionRefund.refundAmount = value1.refundAmount.add(value2.refundAmount);

            return factPromotionRefund;
        }
    }


    public static class NewOrderUserFunction implements ReduceFunction<FactPromotionNewOrderMember> {

        @Override
        public FactPromotionNewOrderMember reduce(FactPromotionNewOrderMember value1, FactPromotionNewOrderMember value2) throws Exception {
            FactPromotionNewOrderMember factPromotionNewOrderMember = new FactPromotionNewOrderMember();
            factPromotionNewOrderMember.shopId = value1.shopId;
            factPromotionNewOrderMember.promotionType = value1.promotionType;
            factPromotionNewOrderMember.newOrderUserCount = value1.newOrderUserCount + value2.newOrderUserCount;

            return factPromotionNewOrderMember;
        }
    }
}
