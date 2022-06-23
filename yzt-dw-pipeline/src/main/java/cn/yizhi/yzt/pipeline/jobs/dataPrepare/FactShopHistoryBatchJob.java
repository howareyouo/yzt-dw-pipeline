package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactShopDayHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactShopHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactShopHourHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactShopMonthHistory;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactShopDayV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactShopHourV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactShopMonthV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactShopV1;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.types.Row;

import java.math.BigDecimal;

public class FactShopHistoryBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> factShopRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactShopHistory.class,
                "fact_shop",
                null)
                .name("ds-fact_shop");

        DataSet<Row> factShopDayRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactShopDayHistory.class,
                "fact_shop_day",
                null)
                .name("ds-fact_shop_day");

        DataSet<Row> factShopHourRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactShopHourHistory.class,
                "fact_shop_hour",
                null)
                .name("ds-fact_shop_hour");

        DataSet<Row> factShopMonthRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactShopMonthHistory.class,
                "fact_shop_month",
                null)
                .name("ds-fact_shop_month");

        OutputFormat<FactShopHourV1> factShopHourV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactShopHourV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_SHOP_HOUR)
                        .finish();

        OutputFormat<FactShopDayV1> factShopDayV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactShopDayV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_SHOP_DAY)
                        .finish();

        OutputFormat<FactShopMonthV1> factShopMonthV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactShopMonthV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_SHOP_MONTH)
                        .finish();

        OutputFormat<FactShopV1> factShopOft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactShopV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_SHOP)
                        .finish();


        factShopHourRow
                .map(new MapFunction<Row, FactShopHourV1>() {
                    @Override
                    public FactShopHourV1 map(Row value) throws Exception {
                        FactShopHourHistory factShopHourHistory = PojoTypes.of(FactShopHourHistory.class).fromRow(value);
                        if ("wxweb".equals(factShopHourHistory.getChannel())) {
                            factShopHourHistory.setChannel("web");
                        }
                        return convertToFactShopHourV1(factShopHourHistory);
                    }
                })
                .filter(new FilterFunction<FactShopHourV1>() {
                    @Override
                    public boolean filter(FactShopHourV1 value) throws Exception {
                        return value.getRowTime() != null && value.getChannel() != null
                                && value.getShopId() != null && value.getPv() != 0
                                && TimeUtil.convertStringHourToLong(value.getRowTime()) <= TimeUtil.convertStringHourToLong(serverConfig.getEndHour());
                    }
                })
                .output(factShopHourV1Oft);


        factShopMonthRow
                .map(new MapFunction<Row, FactShopMonthV1>() {
                    @Override
                    public FactShopMonthV1 map(Row value) throws Exception {
                        FactShopMonthHistory factShopMonthHistory = PojoTypes.of(FactShopMonthHistory.class).fromRow(value);
                        if ("wxweb".equals(factShopMonthHistory.getChannel())) {
                            factShopMonthHistory.setChannel("web");
                        }
                        return convertToFactShopMonthV1(factShopMonthHistory);
                    }
                })
                .filter(new FilterFunction<FactShopMonthV1>() {
                    @Override
                    public boolean filter(FactShopMonthV1 value) throws Exception {
                        return value.getRowTime() != null && value.getChannel() != null
                                && value.getShopId() != null && value.getPv() != 0
                                && TimeUtil.convertStringMonthToLong(value.getRowTime()) <= TimeUtil.convertStringMonthToLong(serverConfig.getEndMonth());
                    }
                })
                .output(factShopMonthV1Oft);

        DataSet<FactShopHistory> factShopHistoryDs = factShopRow
                .map(new MapFunction<Row, FactShopHistory>() {
                    @Override
                    public FactShopHistory map(Row value) throws Exception {
                        FactShopHistory factShopHistory = PojoTypes.of(FactShopHistory.class).fromRow(value);
                        if ("wxweb".equals(factShopHistory.getChannel())) {
                            factShopHistory.setChannel("web");
                        }
                        return factShopHistory;
                    }
                });

        factShopHistoryDs
                .map(new MapFunction<FactShopHistory, FactShopV1>() {
                    @Override
                    public FactShopV1 map(FactShopHistory value) throws Exception {
                        return convertToFactShopV1(value);
                    }
                })
                .filter(new FilterFunction<FactShopV1>() {
                    @Override
                    public boolean filter(FactShopV1 value) throws Exception {
                        return value.getRowTime() != null && value.getChannel() != null
                                && value.getShopId() != null && value.getMemberCount() != 0
                                && TimeUtil.convertStringDayToLong(value.getRowTime()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factShopOft);

        //过滤数据
        DataSet<FactShopDayHistory> shopDayHistoryFilterDs = factShopDayRow
                .map(new MapFunction<Row, FactShopDayHistory>() {
                    @Override
                    public FactShopDayHistory map(Row value) throws Exception {
                        FactShopDayHistory factShopDayHistory = PojoTypes.of(FactShopDayHistory.class).fromRow(value);
                        if ("wxweb".equals(factShopDayHistory.getChannel())) {
                            factShopDayHistory.setChannel("web");
                        }
                        return factShopDayHistory;
                    }
                })
                .filter(new FilterFunction<FactShopDayHistory>() {
                    @Override
                    public boolean filter(FactShopDayHistory value) throws Exception {
                        return value.getShopId() != null && value.getRowDate() != null && value.getChannel() != null
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                });

        factShopHistoryDs
                .filter(new FilterFunction<FactShopHistory>() {
                    @Override
                    public boolean filter(FactShopHistory value) throws Exception {
                        return value.getShopId() != null && value.getRowDate() != null && value.getChannel() != null;
                    }
                })
                .join(shopDayHistoryFilterDs)
                .where(new KeySelector<FactShopHistory, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactShopHistory value) throws Exception {
                        return new Tuple3<>(value.getRowDate(), value.getChannel(), value.getShopId());
                    }
                })
                .equalTo(new KeySelector<FactShopDayHistory, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactShopDayHistory value) throws Exception {
                        return new Tuple3<>(value.getRowDate(), value.getChannel(), value.getShopId());
                    }
                })
                .with(new JoinFunction<FactShopHistory, FactShopDayHistory, FactShopDayV1>() {
                    @Override
                    public FactShopDayV1 join(FactShopHistory first, FactShopDayHistory second) throws Exception {
                        return convertToFactShopDayV1(first, second);
                    }
                })
                .filter(new FilterFunction<FactShopDayV1>() {
                    @Override
                    public boolean filter(FactShopDayV1 value) throws Exception {
                        return value.getRowTime() != null && value.getChannel() != null
                                && value.getShopId() != null;
                    }
                })
                .output(factShopDayV1Oft);

    }

    private static FactShopHourV1 convertToFactShopHourV1(FactShopHourHistory factShopHourHistory) {
        FactShopHourV1 factShopHourV1 = new FactShopHourV1();
        factShopHourV1.setRowTime(factShopHourHistory.getRowTime());
        factShopHourV1.setChannel(factShopHourHistory.getChannel());
        factShopHourV1.setShopId(factShopHourHistory.getShopId());
        factShopHourV1.setPv(factShopHourHistory.getPv() != null ? factShopHourHistory.getPv() : 0);
        factShopHourV1.setUv(factShopHourHistory.getUv() != null ? factShopHourHistory.getUv() : 0);
        factShopHourV1.setAddCartCount(factShopHourHistory.getAddcartCount() != null ? factShopHourHistory.getAddcartCount() : 0);
        factShopHourV1.setAddCartUserCount(factShopHourHistory.getAddcartNumber() != null ? factShopHourHistory.getAddcartNumber() : 0);
        factShopHourV1.setShareCount(factShopHourHistory.getShareCount() != null ? factShopHourHistory.getShareCount() : 0);
        factShopHourV1.setShareUserCount(factShopHourHistory.getShareNumber() != null ? factShopHourHistory.getShareNumber() : 0);
        factShopHourV1.setOrderCount(factShopHourHistory.getOrderCount() != null ? factShopHourHistory.getOrderCount() : 0);
        factShopHourV1.setOrderUserCount(factShopHourHistory.getOrderNumber() != null ? factShopHourHistory.getOrderNumber() : 0);
        factShopHourV1.setPayCount(factShopHourHistory.getPayCount() != null ? factShopHourHistory.getPayCount() : 0);
        factShopHourV1.setPayUserCount(factShopHourHistory.getPayNumber() != null ? factShopHourHistory.getPayNumber() : 0);
        factShopHourV1.setSaleCount(factShopHourHistory.getSaleCount() != null ? factShopHourHistory.getSaleCount() : 0);
        factShopHourV1.setPayAmount(factShopHourHistory.getPayAmount() != null ? factShopHourHistory.getPayAmount() : BigDecimal.ZERO);
        factShopHourV1.setOrderAmount(factShopHourHistory.getOrderAmount() != null ? factShopHourHistory.getOrderAmount() : BigDecimal.ZERO);
        factShopHourV1.setRefundCount(factShopHourHistory.getRefundCount() != null ? factShopHourHistory.getRefundCount() : 0);
        factShopHourV1.setRefundAmount(factShopHourHistory.getRefundAmount() != null ? factShopHourHistory.getRefundAmount() : BigDecimal.ZERO);

        return factShopHourV1;
    }


    private static FactShopMonthV1 convertToFactShopMonthV1(FactShopMonthHistory factShopMonthHistory) {
        FactShopMonthV1 factShopMonthV1 = new FactShopMonthV1();
        factShopMonthV1.setRowTime(factShopMonthHistory.getMonth());
        factShopMonthV1.setShopId(factShopMonthHistory.getShopId());
        factShopMonthV1.setChannel(factShopMonthHistory.getChannel());
        factShopMonthV1.setPv(factShopMonthHistory.getPv() != null ? factShopMonthHistory.getPv() : 0);
        factShopMonthV1.setOrderCount(factShopMonthHistory.getOrderCount() != null ? factShopMonthHistory.getOrderCount() : 0);
        factShopMonthV1.setRegisterCount(factShopMonthHistory.getRegisterCount() != null ? factShopMonthHistory.getRegisterCount() : 0);
        factShopMonthV1.setPayAmount(factShopMonthHistory.getPayAmount() != null ? factShopMonthHistory.getPayAmount() : BigDecimal.ZERO);
        factShopMonthV1.setPayCount(factShopMonthHistory.getPayCount() != null ? factShopMonthHistory.getPayCount() : 0);
        factShopMonthV1.setRefundCount(factShopMonthHistory.getRefundCount() != null ? factShopMonthHistory.getRefundCount() : 0);
        factShopMonthV1.setRefundAmount(factShopMonthHistory.getRefundAmount() != null ? factShopMonthHistory.getRefundAmount() : BigDecimal.ZERO);

        return factShopMonthV1;
    }

    private static FactShopV1 convertToFactShopV1(FactShopHistory factShopHistory) {
        FactShopV1 factShopV1 = new FactShopV1();
        factShopV1.setRowTime(factShopHistory.getRowDate());
        factShopV1.setChannel(factShopHistory.getChannel());
        factShopV1.setShopId(factShopHistory.getShopId());
        factShopV1.setMemberCount(factShopHistory.getAllMember() != null ? factShopHistory.getAllMember() : 0);
        factShopV1.setNotOrderUserCount(factShopHistory.getNotPaiedMember() != null ? factShopHistory.getNotFirstPaiedMember() : 0);
        factShopV1.setOrderAmount(factShopHistory.getAllAmount() != null ? factShopHistory.getAllAmount() : BigDecimal.ZERO);
        factShopV1.setOrderUserCount(factShopHistory.getPaiedMember() != null ? factShopHistory.getPaiedMember() : 0);
        factShopV1.setRefundAmount(factShopHistory.getAllRefundAmount() != null ? factShopHistory.getAllRefundAmount() : BigDecimal.ZERO);

        return factShopV1;
    }

    private static FactShopDayV1 convertToFactShopDayV1(FactShopHistory factShopHistory, FactShopDayHistory factShopDayHistory) {
        FactShopDayV1 factShopDayV1 = new FactShopDayV1();
        factShopDayV1.setRowTime(factShopHistory.getRowDate());
        factShopDayV1.setShopId(factShopHistory.getShopId());
        factShopDayV1.setChannel(factShopHistory.getChannel());
        factShopDayV1.setPv(factShopHistory.getPv() != null ? factShopHistory.getPv() : 0);
        factShopDayV1.setUv(factShopHistory.getUv() != null ? factShopHistory.getUv() : 0);
        factShopDayV1.setShareCount(factShopHistory.getShareCount() != null ? factShopHistory.getShareCount() : 0);
        factShopDayV1.setPayAmount(factShopHistory.getPayAmount() != null ? factShopHistory.getPayAmount() : BigDecimal.ZERO);
        factShopDayV1.setPromotionCount(factShopHistory.getPromotionCount() != null ? factShopHistory.getPromotionCount() : 0);
        factShopDayV1.setPromotionOrderAmount(factShopHistory.getPromotionOrderAmount() != null ? factShopHistory.getPromotionOrderAmount() : BigDecimal.ZERO);
        factShopDayV1.setPromotionDiscountAmount(factShopHistory.getPromotionDiscountAmount() != null ? factShopHistory.getPromotionDiscountAmount() : BigDecimal.ZERO);

        factShopDayV1.setAddCartUserCount(factShopDayHistory.getAddcartMember() != null ? factShopDayHistory.getAddcartMember() : 0);
        factShopDayV1.setOrderCount(factShopDayHistory.getOrderCount() != null ? factShopDayHistory.getOrderCount() : 0);
        factShopDayV1.setOrderUserCount(factShopDayHistory.getOrderMember() != null ? factShopDayHistory.getOrderMember() : 0);
        factShopDayV1.setOrderUserCount(factShopDayHistory.getPayMember() != null ? factShopDayHistory.getPayMember() : 0);
        factShopDayV1.setRegisterCount(factShopDayHistory.getRegisterCount() != null ? factShopDayHistory.getRegisterCount() : 0);
        factShopDayV1.setPayCount(factShopDayHistory.getPayCount() != null ? factShopDayHistory.getPayCount() : 0);
        factShopDayV1.setRefundCount(factShopDayHistory.getRefundCount() != null ? factShopDayHistory.getRefundCount() : 0);
        factShopDayV1.setRefundAmount(factShopDayHistory.getRefundAmount() != null ? factShopDayHistory.getRefundAmount() : BigDecimal.ZERO);
        factShopDayV1.setUvProduct(factShopDayHistory.getUvProduct() != null ? factShopDayHistory.getUvProduct() : 0);

        return factShopDayV1;
    }
}
