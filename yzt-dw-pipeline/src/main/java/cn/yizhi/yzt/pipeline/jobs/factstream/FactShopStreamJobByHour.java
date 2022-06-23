package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.Tables;

/**
 * @author hucheng
 * @date 2020/11/9 下午7:36
 */
public class FactShopStreamJobByHour extends FactShopStreamJob {

    @Override
    public void defineJob() throws Exception {
        defineJobShopJob("yyyy-MM-dd HH", Tables.SINK_TABLE_SHOP_HOUR);
    }
}
