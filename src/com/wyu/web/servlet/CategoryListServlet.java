package com.wyu.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.wyu.domain.Category;
import com.wyu.service.CategoryService;
import com.wyu.utils.JedisPoolUtils;

import redis.clients.jedis.Jedis;

public class CategoryListServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//先从缓存中查找categoryList，如果有直接使用，如果没有再从数据库中查询
		//获得Jedis对象，连接Redis数据库
		Jedis jedis = JedisPoolUtils.getJedis();
		String categoryListJson = jedis.get("categoryListJson");
		
		//判断缓存中是否有数据，如果没有就在数据库中查询数据并存入缓存
		if(categoryListJson==null) {
			//获取导航栏banner上的商品类别
			CategoryService Cservice = new CategoryService();
			List<Category> categoryList = Cservice.findCategory();
			
			//将list转换成json类型
			Gson gson = new Gson();
			categoryListJson = gson.toJson(categoryList);
			System.out.println(123456);  
			//将数据存入缓存中
			jedis.set("categoryListJson", categoryListJson);
		}
		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(categoryListJson);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
