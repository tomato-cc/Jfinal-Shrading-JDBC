package com.demo.common.plugin;

import com.demo.common.ehcache.EhcacheConstant;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author : tomatocc
 * 分表策略
 */
public class HashPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        // 1.获取分表的id(数据表中order_id或applyId等唯一主键)
        String applyId = shardingValue.getValue();
        // 2.从根据key,从缓存中获取真实表名
        String tableName = CacheKit.get(EhcacheConstant.EHCACHE_CONSTANT_SHARDING,applyId);
        if(StrKit.notBlank(tableName)){
            return tableName;
        }
        // 3. 获取逻辑表名(例如：例如真实表：ncoas_apply_log_11,则逻辑表为：ncoas_apply_log)
        String targetName = null;
        for(String each :availableTargetNames){
            targetName = each.substring(0,each.lastIndexOf("_"));
            break;
        }

        // 4.将分表id进行哈希取绝对值后，然后和真实表数进行取余。由于余数不可能大于除数，所以结果范围恰好在0-真实表数
        int node = Math.abs(applyId.hashCode()) % ShardingCacheKit.me().getCache(targetName);
        // 5. 循环去匹配真实表名。匹配后放入分表缓存。
        for(String each :availableTargetNames){
            if(each.endsWith(String.valueOf(node))){
                System.out.println("appLyId : " + applyId + " save table is : " + each);
                CacheKit.put(EhcacheConstant.EHCACHE_CONSTANT_SHARDING, applyId,each);
                return each;
            }
        }

        // 6.异常处理:分表规则计算异常
        System.out.println("Shrading database error . appLyId: " + applyId);
       throw  new UnsupportedOperationException();
    }
}
