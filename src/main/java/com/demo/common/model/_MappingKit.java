package com.demo.common.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * @author : tomatocc
 */
public class _MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("t_blog", "id", Blog.class);
	}
}

