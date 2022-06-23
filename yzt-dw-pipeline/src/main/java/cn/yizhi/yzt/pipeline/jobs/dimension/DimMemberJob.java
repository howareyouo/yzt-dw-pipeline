package cn.yizhi.yzt.pipeline.jobs.dimension;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.model.dim.DimMember;
import cn.yizhi.yzt.pipeline.model.ods.MemberBase;
import cn.yizhi.yzt.pipeline.model.ods.ShopMember;
import cn.yizhi.yzt.pipeline.model.ods.User;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;

import java.sql.Date;
import java.time.Instant;

public class DimMemberJob {

    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSource<Row> odsUser = JdbcDataSourceBuilder.buildDataSource(env,
            serverConfig,
            User.class,
            "ods_user",
            null).name("ds-odsUser");

        DataSource<Row> odsMemberBase = JdbcDataSourceBuilder.buildDataSource(env,
            serverConfig,
            MemberBase.class,
            "ods_member_base",
            null).name("ds-odsMemberBase");


        DataSource<Row> odsShopMember = JdbcDataSourceBuilder.buildDataSource(env,
            serverConfig,
            ShopMember.class,
            "ods_shop_member",
            null).name("ds-odsShopMember");

        // sink
        OutputFormat<DimMember> outputFormat =
            new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimMember.class)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName("dim_member")
                .finish();

        odsShopMember.join(odsMemberBase)
            .where("member_id")
            .equalTo("id")
            .with(new JoinFunction<Row, Row, DimMember>() {
                @Override
                public DimMember join(Row shopMemberRow, Row memberBaseRow) throws Exception {
                    ShopMember shopMember = PojoTypes.of(ShopMember.class).fromRow(shopMemberRow);
                    MemberBase memberBase = PojoTypes.of(MemberBase.class).fromRow(memberBaseRow);

                    DimMember dimMember = new DimMember();

                    dimMember.setMemberId(shopMember.getId());
                    dimMember.setMemberBaseId(shopMember.getMemberId());
                    dimMember.setShopId(shopMember.getShopId());
                    dimMember.setMainShopId(shopMember.getMainShopId());
                    dimMember.setRemark(shopMember.getRemark());
                    dimMember.setSource(shopMember.getSource());
                    dimMember.setDisabled(shopMember.getDisabled() == 1);
                    dimMember.setCreatedAt(shopMember.getCreatedAt());

                    dimMember.setUserId(memberBase.getUserId());
                    dimMember.setName(memberBase.getName());
                    dimMember.setNickname(memberBase.getNickname());
                    dimMember.setAvatar(memberBase.getAvatar());
                    dimMember.setGender(memberBase.getGender() == 0 ? "未知" : (memberBase.getGender() == 1 ? "男" : "女"));
                    dimMember.setWechat(memberBase.getWechat());
                    dimMember.setProvince(memberBase.getProvince());
                    dimMember.setCity(memberBase.getCity());
                    dimMember.setBirthday(memberBase.getBirthday());

                    return dimMember;
                }
            })
            .join(odsUser)
            .where("userId")
            .equalTo("id")
            .with(new JoinFunction<DimMember, Row, DimMember>() {

                @Override
                public DimMember join(DimMember first, Row userRow) throws Exception {
                    User user = PojoTypes.of(User.class).fromRow(userRow);

                    first.setPhone(user.getPhone());
                    first.setCountryCode(user.getCountryCode());
                    first.setStartDate(new Date(Instant.now().toEpochMilli()));
                    first.setEndDate(Date.valueOf("9999-12-31"));

                    return first;
                }
            }).output(outputFormat);


    }
}