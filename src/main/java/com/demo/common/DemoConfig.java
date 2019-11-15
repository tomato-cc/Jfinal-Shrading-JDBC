package com.demo.common;
import java.util.*;

import com.demo.blog.BlogController;
import com.demo.common.model._MappingKit;
import com.demo.common.plugin.HashPreciseShardingAlgorithm;
import com.demo.common.plugin.ShardingCacheKit;
import com.demo.common.plugin.ShardingDruidPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.template.Engine;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;

/**
 * @author : tomatocc
 * 程序入口
 */
public class DemoConfig extends JFinalConfig {
	
	static Prop p;
	
	/**
	 * 启动入口，运行此 main 方法可以启动项目，此 main 方法可以放置在任意的 Class 类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		UndertowServer.start(DemoConfig.class);
	}
	
	/**
	 * PropKit.useFirstFound(...) 使用参数中从左到右最先被找到的配置文件
	 * 从左到右依次去找配置，找到则立即加载并立即返回，后续配置将被忽略
	 */
	static void  loadConfig() {
		if (p == null) {
			p = PropKit.useFirstFound("demo-config-pro.txt", "demo-config-dev.txt");
		}
	}
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		loadConfig();
		
		me.setDevMode(p.getBoolean("devMode", false));
		
		/**
		 * 支持 Controller、Interceptor、Validator 之中使用 @Inject 注入业务层，并且自动实现 AOP
		 * 注入动作支持任意深度并自动处理循环注入
		 */
		me.setInjectDependency(true);
		
		// 配置对超类中的属性进行注入
		me.setInjectSuperClass(true);
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/blog", BlogController.class);
	}
	
	public void configEngine(Engine me) { }
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置 druid 数据库连接池插件
		Map<String,DruidPlugin> dataSourceMap = new HashMap<String, DruidPlugin>();

		DruidPlugin masterPlugin = new DruidPlugin(p.get("master.jdbcUrl"), p.get("master.user"), p.get("master.password").trim());

		dataSourceMap.put("jf_master",masterPlugin);

		// 配置分表规则
		ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
		shardingRuleConfiguration.setDefaultDataSourceName("jf_master");

		shardingRuleConfiguration.setEncryptRuleConfig(getOrderEncryptRuleConfiguration());

		List<TableRuleConfiguration> tableRuleConfigurations = new LinkedList<>();
		// 读取分表配置，生成分表规则
		String sharding = p.get("sharding");
		String [] rule = sharding.split(":");
		if(!Objects.isNull(rule)){
			// 1. 获得真实表数目
			int num = Integer.parseInt(rule[1]);
			// 2.// 获得逻辑表名
			String tableName = rule[0];
			// 3.// 分表字段
			String shardingColumn = rule[2];
			ShardingCacheKit.me().setCache(tableName,num);
			// 4.分表规则和生成分表表达式
			TableRuleConfiguration tableRuleConfiguration =
					new TableRuleConfiguration(tableName,"jf_master." + tableName + "_${0.. " + (num -1 ) +"}");

			// 5.配置分表策略
			StandardShardingStrategyConfiguration shardingStrategyConfiguration =
					new StandardShardingStrategyConfiguration(shardingColumn,new HashPreciseShardingAlgorithm());
			tableRuleConfiguration.setTableShardingStrategyConfig(shardingStrategyConfiguration);
			// 6. 加入策略
			tableRuleConfigurations.add(tableRuleConfiguration);
			shardingRuleConfiguration.setTableRuleConfigs(tableRuleConfigurations);

		}

		Properties props = new Properties();
		props.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(),"true");
		// 加入分表插件
		ShardingDruidPlugin shardingDruidPlugin = new ShardingDruidPlugin(shardingRuleConfiguration,dataSourceMap,props);

		me.add(shardingDruidPlugin);

		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(shardingDruidPlugin);
		// 所有映射在 MappingKit 中自动化搞定
		_MappingKit.mapping(arp);
		me.add(arp);

		// 配置缓存插件
		me.add(new EhCachePlugin());
	}
	
	public static DruidPlugin createDruidPlugin() {
		loadConfig();
		
		return new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		
	}

	/**
	 * 脱敏配置
	 * @return
	 */
	private static EncryptRuleConfiguration getOrderEncryptRuleConfiguration() {
		EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration();
		Properties properties = new Properties();
		// 设置算法的密钥
		properties.setProperty("aes.key.value", "123456");
		// 将逻辑表t_blog的content列进行脱敏，算法采用AES
		EncryptorRuleConfiguration encryptorRuleConfiguration =
				new EncryptorRuleConfiguration("AES", "t_blog.content", properties);
		encryptRuleConfiguration.getEncryptorRuleConfigs().put("user_encryptor", encryptorRuleConfiguration);

		return encryptRuleConfiguration;
	}
}
