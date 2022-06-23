package cn.yizhi.yzt.pipeline.function;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcClient;
import cn.yizhi.yzt.pipeline.model.ods.FlashSaleActivity;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.curator5.com.google.common.cache.Cache;
import org.apache.flink.shaded.curator5.com.google.common.cache.CacheBuilder;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author hucheng
 * 异步查询秒杀相关信息
 * @date 2020/10/26 10:45
 */
public class DimFlashSaleAsyncFunction extends RichAsyncFunction<Integer, FlashSaleActivity> {
    //缓存数据
    private Cache<Integer, FlashSaleActivity> cache;
    private transient JdbcClient jdbcClient;

    private ServerConfig serverConfig;

    public DimFlashSaleAsyncFunction(ServerConfig config) {
        this.serverConfig = config;
    }


    //连接mysql
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        jdbcClient = new JdbcClient(serverConfig);
        //初始化缓存
        cache = CacheBuilder.newBuilder().maximumSize(2).expireAfterAccess(5, TimeUnit.MINUTES).build();
    }


    //异步查询
    @Override
    public void asyncInvoke(Integer input, ResultFuture<FlashSaleActivity> resultFuture) throws Exception {
        //先从缓存中获取
        FlashSaleActivity cacheValue = cache.getIfPresent(input);
        if (cacheValue != null) {
            resultFuture.complete(Collections.singleton(cacheValue));
        } else {
            String sql = "select id,shop_id as shopId,activity_code as activityCode,activity_state as activityState from ods_flashsale_activity where id = ?";
            List<FlashSaleActivity> query = jdbcClient.query(sql, new Object[]{input}, FlashSaleActivity.class);
            if (query != null && query.size() != 0) {
                resultFuture.complete(query);
            }
        }

    }

    @Override
    public void timeout(Integer input, ResultFuture<FlashSaleActivity> resultFuture) throws Exception {

    }

    //关闭连接
    @Override
    public void close() throws Exception {
        super.close();
    }
}
