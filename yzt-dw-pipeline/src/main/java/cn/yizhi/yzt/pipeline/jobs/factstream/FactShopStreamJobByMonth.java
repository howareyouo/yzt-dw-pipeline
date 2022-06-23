package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.Tables;

/**
 * @author hucheng
 * @date 2020/11/9 下午7:45
 */
public class FactShopStreamJobByMonth extends FactShopStreamJob {
    @Override
    public void defineJob() throws Exception {
        defineJobShopJob("yyyy-MM", Tables.SINK_TABLE_SHOP_MONTH);
    }
}
