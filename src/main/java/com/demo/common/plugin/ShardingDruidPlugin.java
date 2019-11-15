package com.demo.common.plugin;
import java.sql.SQLException;
import	java.util.HashMap;
import	java.util.Properties;

import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.druid.DruidPlugin;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 分表插件数据源配置
 */
public class ShardingDruidPlugin implements IPlugin, IDataSourceProvider {

    //分表规则
    private ShardingRuleConfiguration shardingRuleConfiguration;

    // 数据源map
    private Map<String, DruidPlugin> dataMap;

    // 原数据源map
    private Map < String, DataSource> dataSource;

    //sharding-jdbc封装后的数据源
    private DataSource data;

    // 相关配置
    private Properties properties;

    public  ShardingDruidPlugin(ShardingRuleConfiguration shardingDruidPlugin, Properties properties){
        this(shardingDruidPlugin,null ,properties);
    }

    public ShardingDruidPlugin(ShardingRuleConfiguration shardingDruidPlugin,Map < String, DruidPlugin> dataSource){
        this(shardingDruidPlugin,dataSource, null);
    }

    public ShardingDruidPlugin(ShardingRuleConfiguration shardingDruidPlugin,Map < String, DruidPlugin> dataMap ,Properties properties){
        this.shardingRuleConfiguration = shardingDruidPlugin;
        this.dataMap = dataMap;
        dataSource = new HashMap<String, DataSource> ();
        if(properties == null){
            properties = new Properties();
        } else {
            this.properties = properties;
        }
    }

    @Override
    public boolean start() {
        // 遍历所有数据源，加入到sharding-jdbc
        for (Map.Entry < String, DruidPlugin > entry:dataMap.entrySet()) {
                entry.getValue().start();
            dataSource.put(entry.getKey(),entry.getValue().getDataSource());
        }

        try {
            // sharding-jdbc进行接管数据源
            data = ShardingDataSourceFactory.createDataSource(dataSource,shardingRuleConfiguration,properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean stop() {
        // 移除被sharding-jdbc接管的数据源
        for (Map.Entry < String, DruidPlugin > entry:dataMap.entrySet()) {
            entry.getValue().stop();
            dataSource.remove(entry.getKey());
        }
        return true;
    }

    @Override
    public DataSource getDataSource() {
        return data;
    }
}
