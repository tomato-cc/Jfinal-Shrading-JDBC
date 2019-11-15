package com.demo.blog;

import com.demo.common.model.Blog;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author : tomatocc
 * BlogService
 */
public class BlogService {
	
	private Blog dao = new Blog().dao();

	public boolean save(Blog blog){
		return blog.save();
	}
}
