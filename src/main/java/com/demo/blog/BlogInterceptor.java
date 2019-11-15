package com.demo.blog;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * @author : tomatocc
 * BlogInterceptor
 * 此拦截器仅做为示例展示
 */
public class BlogInterceptor implements Interceptor {
	
	public void intercept(Invocation inv) {
		System.out.println("Before invoking " + inv.getActionKey());
		inv.invoke();
		System.out.println("After invoking " + inv.getActionKey());
	}
}
