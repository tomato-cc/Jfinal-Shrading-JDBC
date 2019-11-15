package com.demo.common.plugin;
import	java.util.concurrent.ConcurrentHashMap;

/**
 * 分表缓存kit
 */
public class ShardingCacheKit {
    private ShardingCacheKit(){};

    private ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<String, Integer> ();

    private  static ShardingCacheKit cacheKit = new ShardingCacheKit();

    public  static ShardingCacheKit me () {return cacheKit;}

    /**
     * set到map
     * @param tableName
     * @param num
     */
    public void setCache(String tableName, Integer num){
        cache.putIfAbsent(tableName, num);
    }

    /**
     * 从map取表数
     * @param tableName
     * @return
     */
    public int getCache(String tableName){
        if(cache.containsKey(tableName)) {
            return cache.get(tableName);
        }
        return 0;
    }
}
