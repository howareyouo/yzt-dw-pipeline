package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.Tables;

/**
 * @author hucheng
 * @date 2020/11/6 17:52
 */
public class FactShopStreamJobByDay extends  FactShopStreamJob{
    @Override
    public void defineJob() throws Exception {
        defineJobShopJob("yyyy-MM-dd", Tables.SINK_TABLE_SHOP_DAY);
    }
}
