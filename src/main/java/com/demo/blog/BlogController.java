package com.demo.blog;

import com.demo.common.ehcache.EhcacheConstant;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.demo.common.model.Blog;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;

import java.util.HashMap;
import java.util.Map;

/**
 *测试分表的Controller
 */
@Before(BlogInterceptor.class)
public class BlogController extends Controller {
	
	@Inject
	BlogService service;

	public void save() {
		Blog bl = new Blog();
		bl.setId(StrKit.getRandomUUID());
		bl.setContent("content");
		bl.setTitle("title");
		service.save(bl);

		Map<String ,Object> map = new HashMap<String ,Object>();
		map.put("table",CacheKit.get(EhcacheConstant.EHCACHE_CONSTANT_SHARDING, bl.getId()));
		map.put("id",bl.getId());

		renderJson(Ret.ok("msg" , map));
	}
}


