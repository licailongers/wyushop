package com.wyu.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.wyu.dao.AdminDao;
import com.wyu.domain.Category;
import com.wyu.domain.Order;
import com.wyu.domain.Product;

public class AdminService {

	public List<Category> findAllCategory() {
		AdminDao dao = new AdminDao();
		try {
			return dao.findAllCategory();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void saveProduct(Product product) throws SQLException {
		AdminDao dao = new AdminDao();
		dao.saveProduct(product);
	}

	public List<Order> findAllOrders() {
		AdminDao dao = new AdminDao();
		List<Order> ordersList = null;
		try {
			ordersList = dao.findAllOrders();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ordersList;
	}

	public List<Map<String, Object>> findOrderInfoByOid(String oid) {
		AdminDao dao = new AdminDao();
		List<Map<String, Object>> mapList = null;
		try {
			mapList = dao.findOrderInfoByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mapList;
	}
	
	public List<Product> findAllProduct() {
		AdminDao dao = new AdminDao();
		List<Product> productList = null;
		try {
			productList = dao.findAllProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return productList;
	}
	
	public void delProductById(String pid) {
		AdminDao dao = new AdminDao();
		try {
			dao.delProductById(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Product findProductById(String pid) {
		AdminDao dao = new AdminDao();
		Product product = null;
		try {
			product = dao.findProductById(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return product;
	}

	public void updateProduct(Product product) {
		AdminDao dao = new AdminDao();
		try {
			dao.updateProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 添加商品类别
	public void addCategory(Category category) {
		AdminDao dao = new AdminDao();
		try {
			dao.addCategory(category);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 根据商品类别ID删除该商品类别
	public void delCategoryByCid(String cid) {
		AdminDao dao = new AdminDao();
		try {
			dao.delCategoryByCid(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 根据cid查找category
	public Category findCategoryByCid(String cid) {
		AdminDao dao = new AdminDao();
		Category category = null;
		try {
			category = dao.findCategoryByCid(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return category;
	}

	// 编辑category
	public void eidtCategory(String cid, String cname) {
		AdminDao dao = new AdminDao();
		try {
			dao.eidtCategory(cid,cname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
