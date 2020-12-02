package com.wyu.service;

import java.sql.SQLException;
import java.util.List;

import com.wyu.dao.CategoryDao;
import com.wyu.domain.Category;

public class CategoryService {

	//获取商品类别
	public List<Category> findCategory() {
		CategoryDao dao = new CategoryDao();
		List<Category> categoryList = null;
		try {
			categoryList = dao.findCategory();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categoryList;
	}

}
